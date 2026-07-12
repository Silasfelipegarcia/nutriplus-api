#!/usr/bin/env python3
"""
Valida todos os usuários do catálogo dev (@nutriplus.local) contra a API em execução.

Requer API com SPRING_PROFILES_ACTIVE=local,dev e usuários já seedados.

Uso:
  python3 scripts/functional_dev_users_validate.py
  python3 scripts/functional_dev_users_validate.py --base-url http://localhost:8080
"""
from __future__ import annotations

import argparse
import json
import sys
import urllib.error
import urllib.request
from dataclasses import dataclass
from typing import Any

PASSWORD = "Nutri123!"

SPECS: list[dict[str, Any]] = [
    {"email": "teste@nutriplus.local", "with_profile": True, "has_plan": False, "first_plan": True},
    {"email": "teste2@nutriplus.local", "with_profile": False},
    {"email": "admin@nutriplus.local", "admin": True},
    {"email": "persona.luna@nutriplus.local", "agent": "LUNA", "status": "ACTIVE"},
    {"email": "persona.bruno@nutriplus.local", "agent": "BRUNO", "status": "ACTIVE"},
    {"email": "plano.essencial@nutriplus.local", "status": "ACTIVE", "plan": "ESSENTIAL_MONTHLY"},
    {"email": "plano.essencial.anual@nutriplus.local", "status": "ACTIVE", "plan": "ESSENTIAL_YEARLY"},
    {"email": "plano.atleta@nutriplus.local", "status": "ACTIVE", "plan": "ATHLETE_MONTHLY", "athlete": True},
    {"email": "plano.atleta.anual@nutriplus.local", "status": "ACTIVE", "plan": "ATHLETE_YEARLY", "athlete": True},
    {"email": "plano.teste@nutriplus.local", "status": "ACTIVE", "plan": "TEST_MONTHLY"},
    {"email": "trial@nutriplus.local", "status": "TRIAL", "em_trial": True},
    {"email": "plano.expirado@nutriplus.local", "status": "EXPIRED"},
    {"email": "helena@nutriplus.local", "age": 68, "life_stage": "SENIOR"},
    {"email": "flora@nutriplus.local", "diet": "VEGETARIAN"},
    {"email": "plano.bloqueado@nutriplus.local", "has_plan": True, "correction": True, "locked": True},
    {"email": "correcao.usada@nutriplus.local", "has_plan": True, "correction": False, "locked": True},
    {"email": "ciclo.vencido@nutriplus.local", "has_plan": True, "review_due": True},
    {"email": "atleta.regen@nutriplus.local", "has_plan": True, "athlete_regen": True},
]


@dataclass
class ApiClient:
    base_url: str

    def _request(self, method: str, path: str, token: str | None = None, body: dict | None = None) -> tuple[int, Any]:
        url = f"{self.base_url.rstrip('/')}{path}"
        data = json.dumps(body).encode() if body is not None else None
        headers = {"Content-Type": "application/json", "Accept": "application/json"}
        if token:
            headers["Authorization"] = f"Bearer {token}"
        req = urllib.request.Request(url, data=data, headers=headers, method=method)
        try:
            with urllib.request.urlopen(req, timeout=15) as resp:
                raw = resp.read().decode()
                return resp.status, json.loads(raw) if raw else None
        except urllib.error.HTTPError as e:
            raw = e.read().decode()
            try:
                payload = json.loads(raw) if raw else None
            except json.JSONDecodeError:
                payload = raw
            return e.code, payload

    def login(self, email: str, password: str) -> str:
        status, data = self._request("POST", "/auth/login", body={"email": email, "password": password})
        if status != 200 or not isinstance(data, dict) or "token" not in data:
            raise RuntimeError(f"login failed for {email}: {status} {data}")
        return data["token"]


def assert_spec(client: ApiClient, spec: dict[str, Any]) -> list[str]:
    errors: list[str] = []
    email = spec["email"]
    try:
        token = client.login(email, PASSWORD)
    except RuntimeError as e:
        return [str(e)]

    if spec.get("admin"):
        status, _ = client._request("GET", "/admin/access/summary", token)
        if status != 200:
            errors.append(f"{email}: admin summary -> {status}")
        return errors

    status, profile = client._request("GET", "/nutrition-profile", token)
    if not spec.get("with_profile", True):
        if status != 404:
            errors.append(f"{email}: expected 404 profile, got {status}")
        return errors

    if status != 200:
        errors.append(f"{email}: profile -> {status}")
        return errors

    if spec.get("agent") and profile.get("agentPersona") != spec["agent"]:
        errors.append(f"{email}: agent {profile.get('agentPersona')} != {spec['agent']}")
    if spec.get("age") and profile.get("age") != spec["age"]:
        errors.append(f"{email}: age {profile.get('age')} != {spec['age']}")
    if spec.get("life_stage") and profile.get("lifeStage") != spec["life_stage"]:
        errors.append(f"{email}: lifeStage {profile.get('lifeStage')} != {spec['life_stage']}")
    if spec.get("diet") and profile.get("dietaryPreference") != spec["diet"]:
        errors.append(f"{email}: diet {profile.get('dietaryPreference')} != {spec['diet']}")

    sub = profile.get("subscriptionStatus") or {}
    if spec.get("status") and sub.get("status") != spec["status"]:
        errors.append(f"{email}: subscription {sub.get('status')} != {spec['status']}")
    if spec.get("plan") and sub.get("plan") != spec["plan"]:
        errors.append(f"{email}: plan {sub.get('plan')} != {spec['plan']}")
    if spec.get("em_trial") and not sub.get("emTrial"):
        errors.append(f"{email}: expected emTrial=true")

    if spec.get("athlete") and not profile.get("athleteModeEnabled"):
        errors.append(f"{email}: athleteModeEnabled expected true")

    plan_status, plan = client._request("GET", "/meal-plans/latest", token)
    wants_plan = spec.get("has_plan", False)
    if wants_plan and plan_status != 200:
        errors.append(f"{email}: meal plan expected 200, got {plan_status}")
    if not wants_plan and spec.get("with_profile", True) and not spec.get("first_plan") and plan_status == 200:
        pass  # ok if has plan unexpectedly
    if spec.get("first_plan") and plan_status != 404:
        errors.append(f"{email}: first plan user expected 404 latest, got {plan_status}")

    elig_status, elig = client._request("GET", "/meal-plans/regeneration-eligibility", token)
    if elig_status != 200:
        errors.append(f"{email}: eligibility -> {elig_status}")
        return errors

    if spec.get("first_plan"):
        if "FIRST_PLAN" not in (elig.get("allowedReasons") or []):
            errors.append(f"{email}: FIRST_PLAN not in allowedReasons")
    if spec.get("has_plan") and not elig.get("hasMealPlan"):
        errors.append(f"{email}: hasMealPlan expected true")
    if spec.get("correction") is True and not elig.get("oneTimeCorrectionAvailable"):
        errors.append(f"{email}: oneTimeCorrectionAvailable expected true")
    if spec.get("correction") is False and elig.get("oneTimeCorrectionAvailable"):
        errors.append(f"{email}: oneTimeCorrectionAvailable expected false")
    if spec.get("locked") and elig.get("daysUntilUnlock", 0) <= 0:
        errors.append(f"{email}: daysUntilUnlock expected > 0")
    if spec.get("review_due") and not elig.get("reviewDue"):
        errors.append(f"{email}: reviewDue expected true")
    if spec.get("athlete_regen") and not elig.get("athleteRegenAvailable"):
        errors.append(f"{email}: athleteRegenAvailable expected true")

    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate dev functional test users against running API")
    parser.add_argument("--base-url", default="http://localhost:8080")
    args = parser.parse_args()

    client = ApiClient(args.base_url)
    health_status, _ = client._request("GET", "/health")
    if health_status != 200:
        print(f"API not healthy at {args.base_url} (status {health_status})", file=sys.stderr)
        return 1

    all_errors: list[str] = []
    for spec in SPECS:
        errs = assert_spec(client, spec)
        if errs:
            all_errors.extend(errs)
        else:
            print(f"OK  {spec['email']}")

    if all_errors:
        print("\nFAILURES:", file=sys.stderr)
        for e in all_errors:
            print(f"  - {e}", file=sys.stderr)
        return 1

    print(f"\nAll {len(SPECS)} dev users validated.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
