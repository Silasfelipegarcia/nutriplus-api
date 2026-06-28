#!/usr/bin/env python3
"""Audit Nutri+ API endpoints against a live base URL with multi-sample latency."""
from __future__ import annotations

import argparse
import json
import os
import statistics
import sys
import time
import uuid
from pathlib import Path
from urllib import error, request

DEFAULT_BASE = "https://nutriplus-api-production.up.railway.app"
LOCAL_TEST_EMAIL = "teste@nutriplus.local"
LOCAL_TEST_PASSWORD = "Nutri123!"
TIMEOUT = 30
SAMPLES = 3


def http(method, path, token=None, body=None, idempotency=False):
    url = f"{BASE}{path}"
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if idempotency:
        headers["Idempotency-Key"] = str(uuid.uuid4())
    data = json.dumps(body).encode() if body is not None else None
    req = request.Request(url, data=data, headers=headers, method=method)
    start = time.perf_counter()
    try:
        with request.urlopen(req, timeout=TIMEOUT) as resp:
            elapsed_ms = (time.perf_counter() - start) * 1000
            raw = resp.read().decode("utf-8", errors="replace")
            return resp.status, elapsed_ms, raw, resp.headers.get("Cache-Control", "")
    except error.HTTPError as e:
        elapsed_ms = (time.perf_counter() - start) * 1000
        raw = e.read().decode("utf-8", errors="replace")
        return e.code, elapsed_ms, raw, e.headers.get("Cache-Control", "") if e.headers else ""


def register_user():
    email = f"audit-{uuid.uuid4().hex[:12]}@nutriplus.test"
    password = "AuditNutri123!"
    status, _, raw, _ = http(
        "POST",
        "/auth/register",
        body={
            "name": "Audit User",
            "email": email,
            "password": password,
            "cpf": "529.982.247-25",
            "birthDate": "1990-06-15",
            "contactPhone": "11999999999",
        },
        idempotency=True,
    )
    if status not in (200, 201):
        raise RuntimeError(f"register failed {status}: {raw[:200]}")
    auth = json.loads(raw)
    return email, auth["token"], auth.get("refreshToken", "")


def login_user(email, password):
    status, _, raw, _ = http("POST", "/auth/login", body={"email": email, "password": password})
    if status != 200:
        raise RuntimeError(f"login failed {status}: {raw[:200]}")
    auth = json.loads(raw)
    return auth["token"], auth.get("refreshToken", "")


def obtain_auth():
    email = os.environ.get("PERF_TEST_EMAIL")
    password = os.environ.get("PERF_TEST_PASSWORD")
    if email and password:
        token, refresh = login_user(email, password)
        return email, token, refresh
    if "localhost" in BASE or "127.0.0.1" in BASE:
        token, refresh = login_user(LOCAL_TEST_EMAIL, LOCAL_TEST_PASSWORD)
        return LOCAL_TEST_EMAIL, token, refresh
    return None, None, None


def grade(status, note):
    if status == 0:
        return "ERRO"
    if status < 400:
        return "OK"
    if note == "pro-only" and status == 403:
        return "OK (403 esperado)"
    if note == "mutation" and status in (400, 404, 409, 422):
        return "OK (sem dados)"
    if note == "optional" and status == 404:
        return "OK (404 esperado)"
    if status == 401:
        return "FALHA AUTH"
    if status >= 500:
        return "FALHA SERVER"
    return f"AVISO ({status})"


def percentile(values, pct):
    if not values:
        return 0
    ordered = sorted(values)
    idx = min(len(ordered) - 1, int(len(ordered) * pct))
    return ordered[idx]


def measure(method, path, tok, body, idem, note, samples):
    latencies = []
    status = 0
    cache = ""
    raw = ""
    for i in range(samples):
        status, ms, raw, cache = http(method, path, tok, body, idem and i == 0)
        latencies.append(ms)
        if note == "mutation" and i == 0:
            break
    return {
        "status": status,
        "ms": round(statistics.mean(latencies)),
        "p50_ms": round(percentile(latencies, 0.5)),
        "p95_ms": round(percentile(latencies, 0.95)),
        "samples": len(latencies),
        "cacheControl": cache or None,
        "grade": grade(status, note),
        "raw_preview": raw[:120] if status >= 400 else None,
    }


def build_endpoints():
    return [
        ("GET", "/health", None, False, "public", "S", "Health check"),
        ("GET", "/training/sports", None, False, "public", "S", "Catálogo esportes"),
        ("GET", "/legal/terms", None, False, "public", "S", "Termos de uso"),
        ("GET", "/legal/privacy", None, False, "public", "S", "Privacidade"),
        ("GET", "/legal/ai-disclosure", None, False, "public", "S", "Divulgação IA"),
        ("GET", "/legal/data-sharing-consent", None, False, "public", "S", "Consentimento dados"),
        ("GET", "/pricing/guidelines", None, False, "public", "S", "Preços marketplace"),
        ("GET", "/nutritionists", None, False, "public", "S", "Lista nutricionistas"),
        ("GET", "/plans", None, False, "public", "A", "Planos assinatura"),
        ("GET", "/feature-flags", None, False, "public", "S", "Feature flags"),
        ("GET", "/users/me", "token", False, "tier-s", "S", "Perfil usuário"),
        ("GET", "/app/bootstrap", "token", False, "optional", "S", "Bootstrap dashboard"),
        ("GET", "/nutrition-profile", "token", False, "optional", "S", "Perfil nutricional"),
        ("GET", "/meal-plans/generation-status", "token", False, "tier-s", "S", "Status geração plano"),
        ("GET", "/meal-plans/latest", "token", False, "optional", "S", "Plano alimentar"),
        ("GET", "/shopping-list/latest", "token", False, "optional", "S", "Lista de compras"),
        ("GET", "/checkins/today", "token", False, "tier-s", "S", "Check-in hoje"),
        ("GET", "/checkins/stats", "token", False, "tier-s", "S", "Stats check-in"),
        ("GET", "/checkins/adherence", "token", False, "tier-s", "S", "Adesão check-in"),
        ("GET", "/progress/schedule", "token", False, "optional", "S", "Cronograma progresso"),
        ("GET", "/progress/measurements/latest", "token", False, "optional", "S", "Última medição"),
        ("GET", "/progress/reviews/latest", "token", False, "optional", "S", "Última revisão"),
        ("GET", "/progress/evolution", "token", False, "optional", "S", "Evolução"),
        ("GET", "/training/profile", "token", False, "optional", "S", "Perfil treino"),
        ("GET", "/feedback/app/latest", "token", False, "optional", "S", "Feedback app"),
        ("GET", "/care/my", "token", False, "tier-s", "S", "Relacionamentos care"),
        ("GET", "/conversations", "token", False, "tier-s", "S", "Conversas"),
        ("GET", "/pro/dashboard", "token", False, "pro-only", "S", "Dashboard pro"),
        ("GET", "/pro/patients", "token", False, "pro-only", "S", "Pacientes pro"),
        ("POST", "/auth/refresh", None, False, "auth", "A", "Refresh token"),
        ("POST", "/onboarding/complete", "token", True, "mutation", "B", "Completar onboarding"),
        ("POST", "/meal-plans/generate", "token", True, "mutation", "C", "Gerar plano"),
        ("POST", "/checkins", "token", True, "mutation", "A", "Salvar check-in"),
        ("POST", "/analytics/events", "token", True, "mutation", "A", "Analytics"),
    ]


def main():
    global BASE
    parser = argparse.ArgumentParser(description="Audit Nutri+ API endpoints")
    parser.add_argument("base_url", nargs="?", default=DEFAULT_BASE)
    parser.add_argument("--env", default="prod", choices=["local", "homolog", "prod"])
    parser.add_argument("--samples", type=int, default=SAMPLES)
    parser.add_argument("--skip-mutations", action="store_true")
    parser.add_argument("--out", default="")
    args = parser.parse_args()

    BASE = args.base_url.rstrip("/")
    out_path = Path(args.out) if args.out else Path("perf/results") / f"audit-{args.env}-{time.strftime('%Y%m%d-%H%M%S')}.json"
    out_path.parent.mkdir(parents=True, exist_ok=True)

    print("=== Nutri+ API — Auditoria de Endpoints ===")
    print(f"Base: {BASE}")
    print(f"Env: {args.env}")
    print(f"Samples: {args.samples}\n")

    email, token, refresh = obtain_auth()
    if token:
        print(f"Usuário de teste: {email}\n")
    else:
        print("Sem credenciais autenticadas — apenas endpoints públicos.\n")
        print("Defina PERF_TEST_EMAIL/PERF_TEST_PASSWORD para audit remoto autenticado.\n")

    bodies = {
        "/onboarding/complete": {
            "nutritionProfile": {
                "sex": "MALE",
                "age": 30,
                "heightCm": 175,
                "weightKg": 75,
                "goal": "MAINTAIN",
                "activityLevel": "MODERATE",
            },
            "athleteModeEnabled": False,
            "activities": [],
        },
        "/checkins": {"mealId": "breakfast", "status": "DONE"},
        "/analytics/events": {"events": [{"name": "audit_ping", "properties": {}}]},
        "/auth/refresh": None,
    }

    print(f"{'Método':<6} {'Endpoint':<38} {'Tier':<4} {'HTTP':<5} {'p50':>6} {'p95':>6}  {'Avaliação':<18} Descrição")
    print("-" * 120)

    results = []
    nutritionist_id = None

    for method, path, tok_key, idem, note, tier, desc in build_endpoints():
        if args.skip_mutations and note == "mutation":
            continue
        tok = token if tok_key == "token" else None
        if tok_key == "token" and not token:
            continue
        body = bodies.get(path)
        if path == "/auth/refresh":
            body = {"refreshToken": refresh}

        measured = measure(method, path, tok, body, idem, note, args.samples)
        print(
            f"{method:<6} {path:<38} {tier:<4} {measured['status']:<5} "
            f"{measured['p50_ms']:>6} {measured['p95_ms']:>6}  {measured['grade']:<18} {desc}"
        )
        results.append(
            {
                "method": method,
                "path": path,
                "tier": tier,
                "note": note,
                "description": desc,
                **measured,
            }
        )

        if path == "/nutritionists" and measured["status"] == 200:
            try:
                status, _, raw, _ = http("GET", path)
                items = json.loads(raw)
                if items:
                    nutritionist_id = items[0]["id"]
            except Exception:
                pass

    if nutritionist_id:
        for suffix in (f"/nutritionists/{nutritionist_id}", f"/nutritionists/{nutritionist_id}/ratings"):
            measured = measure("GET", suffix, None, None, False, "public", args.samples)
            print(
                f"{'GET':<6} {suffix:<38} {'S':<4} {measured['status']:<5} "
                f"{measured['p50_ms']:>6} {measured['p95_ms']:>6}  {measured['grade']:<18} Detalhe marketplace"
            )
            results.append(
                {
                    "method": "GET",
                    "path": suffix,
                    "tier": "S",
                    "note": "public",
                    "description": "Detalhe marketplace",
                    **measured,
                }
            )

    tier_s = [r for r in results if r["tier"] == "S" and r["status"] == 200]
    latencies = [r["p95_ms"] for r in tier_s]
    avg = statistics.mean(latencies) if latencies else 0
    p95 = percentile(latencies, 0.95) if latencies else 0
    dashboard_paths = {
        "/users/me",
        "/nutrition-profile",
        "/meal-plans/latest",
        "/shopping-list/latest",
        "/checkins/today",
        "/checkins/stats",
        "/progress/schedule",
    }
    dash = [r["p95_ms"] for r in results if r["path"] in dashboard_paths and r["status"] in (200, 404)]
    dash_sum = sum(dash)

    print("\n=== Resumo ===")
    print(f"Total testado: {len(results)}")
    print(f"Tier S HTTP 200: {len(tier_s)}")
    print(f"Latência Tier S — média p95: {avg:.0f}ms | p95 agregado: {p95}ms")
    print(f"Fluxo dashboard (soma p95 GETs): {dash_sum}ms")
    failures = [r for r in results if "FALHA" in r["grade"]]
    print(f"Falhas críticas: {len(failures)}")
    for f in failures:
        print(f"  - {f['method']} {f['path']}: {f['grade']}")

    payload = {
        "base": BASE,
        "env": args.env,
        "email": email,
        "results": results,
        "summary": {
            "tier_s_count": len(tier_s),
            "tier_s_avg_p95_ms": round(avg),
            "tier_s_p95_ms": p95,
            "dashboard_flow_sum_p95_ms": dash_sum,
            "critical_failures": len(failures),
        },
    }
    out_path.write_text(json.dumps(payload, indent=2))
    print(f"\nRelatório JSON: {out_path}")


if __name__ == "__main__":
    main()
