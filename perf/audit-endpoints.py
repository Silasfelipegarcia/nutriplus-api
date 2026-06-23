#!/usr/bin/env python3
"""Audit Nutri+ API endpoints against a live base URL."""
import json
import sys
import time
import uuid
from urllib import error, request

BASE = sys.argv[1] if len(sys.argv) > 1 else "https://nutriplus-api-production.up.railway.app"
TIMEOUT = 30


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
        body={"name": "Audit User", "email": email, "password": password},
        idempotency=True,
    )
    if status not in (200, 201):
        raise RuntimeError(f"register failed {status}: {raw[:200]}")
    auth = json.loads(raw)
    return email, auth["token"], auth.get("refreshToken", "")


def grade(status, note):
    if status == 0:
        return "ERRO"
    if status < 400:
        return "OK"
    if note == "pro-only" and status == 403:
        return "OK (403 esperado)"
    if note == "mutation" and status in (400, 404, 409, 422):
        return "OK (sem dados)"
    if status == 401:
        return "FALHA AUTH"
    if status >= 500:
        return "FALHA SERVER"
    return f"AVISO ({status})"


def main():
    print("=== Nutri+ API — Auditoria de Endpoints ===")
    print(f"Base: {BASE}\n")

    email, token, refresh = register_user()
    print(f"Usuário de teste: {email}\n")

    endpoints = [
        ("GET", "/health", None, False, "public", "Health check"),
        ("GET", "/training/sports", None, False, "public", "Catálogo esportes"),
        ("GET", "/legal/terms", None, False, "public", "Termos de uso"),
        ("GET", "/legal/privacy", None, False, "public", "Privacidade"),
        ("GET", "/legal/ai-disclosure", None, False, "public", "Divulgação IA"),
        ("GET", "/legal/data-sharing-consent", None, False, "public", "Consentimento dados"),
        ("GET", "/pricing/guidelines", None, False, "public", "Preços marketplace"),
        ("GET", "/nutritionists", None, False, "public", "Lista nutricionistas"),
        ("GET", "/users/me", token, False, "tier-s", "Perfil usuário"),
        ("GET", "/nutrition-profile", token, False, "tier-s", "Perfil nutricional"),
        ("GET", "/meal-plans/generation-status", token, False, "tier-s", "Status geração plano"),
        ("GET", "/meal-plans/latest", token, False, "tier-s", "Plano alimentar"),
        ("GET", "/shopping-list/latest", token, False, "tier-s", "Lista de compras"),
        ("GET", "/checkins/today", token, False, "tier-s", "Check-in hoje"),
        ("GET", "/checkins/stats", token, False, "tier-s", "Stats check-in"),
        ("GET", "/progress/schedule", token, False, "tier-s", "Cronograma progresso"),
        ("GET", "/progress/measurements/latest", token, False, "tier-s", "Última medição"),
        ("GET", "/progress/reviews/latest", token, False, "tier-s", "Última revisão"),
        ("GET", "/progress/evolution", token, False, "tier-s", "Evolução"),
        ("GET", "/training/profile", token, False, "tier-s", "Perfil treino"),
        ("GET", "/feedback/app/latest", token, False, "tier-s", "Feedback app"),
        ("GET", "/care/my", token, False, "tier-s", "Relacionamentos care"),
        ("GET", "/conversations", token, False, "tier-s", "Conversas"),
        ("POST", "/onboarding/complete", token, True, "mutation", "Completar onboarding"),
        ("POST", "/meal-plans/generate", token, True, "mutation", "Gerar plano"),
        ("POST", "/checkins", token, True, "mutation", "Salvar check-in"),
        ("POST", "/analytics/events", token, True, "mutation", "Analytics"),
        ("GET", "/pro/dashboard", token, False, "pro-only", "Dashboard pro"),
        ("GET", "/pro/patients", token, False, "pro-only", "Pacientes pro"),
        ("POST", "/auth/refresh", None, False, "auth", "Refresh token"),
    ]

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

    print(f"{'Método':<6} {'Endpoint':<42} {'HTTP':<5} {'ms':>6}  {'Avaliação':<18} Descrição")
    print("-" * 115)

    results = []
    for method, path, tok, idem, note, desc in endpoints:
        body = bodies.get(path)
        if path == "/auth/refresh":
            body = {"refreshToken": refresh}
        status, ms, raw, cache = http(method, path, tok, body, idem)
        g = grade(status, note)
        print(f"{method:<6} {path:<42} {status:<5} {ms:>6.0f}  {g:<18} {desc}")
        results.append(
            {
                "method": method,
                "path": path,
                "status": status,
                "ms": round(ms),
                "grade": g,
                "note": note,
                "description": desc,
                "cacheControl": cache or None,
            }
        )

    tier_s = [r for r in results if r["note"] == "tier-s"]
    ok_tier = [r for r in tier_s if r["status"] == 200]
    latencies = sorted(r["ms"] for r in tier_s)
    avg = sum(latencies) / len(latencies) if latencies else 0
    p95 = latencies[int(len(latencies) * 0.95)] if latencies else 0

    print("\n=== Resumo ===")
    print(f"Total testado: {len(results)}")
    print(f"Tier S portal (GET autenticado): {len(ok_tier)}/{len(tier_s)} com HTTP 200")
    print(f"Latência Tier S — média: {avg:.0f}ms | p95: {p95}ms")
    failures = [r for r in results if "FALHA" in r["grade"]]
    if failures:
        print(f"Falhas críticas: {len(failures)}")
        for f in failures:
            print(f"  - {f['method']} {f['path']}: {f['grade']}")
    else:
        print("Falhas críticas: 0")

    out = "/tmp/nutriplus-audit.json"
    with open(out, "w") as f:
        json.dump({"base": BASE, "email": email, "results": results, "summary": {
            "tier_s_ok": f"{len(ok_tier)}/{len(tier_s)}",
            "tier_s_avg_ms": round(avg),
            "tier_s_p95_ms": p95,
        }}, f, indent=2)
    print(f"\nRelatório JSON: {out}")


if __name__ == "__main__":
    main()
