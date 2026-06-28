#!/usr/bin/env bash
# Smoke checks antes de liberar web em produção.
# Uso: API_URL=https://nutriplus-api-production.up.railway.app ./scripts/launch-smoke.sh

set -euo pipefail

API_URL="${API_URL:-https://nutriplus-api-production.up.railway.app}"
WEB_URL="${WEB_URL:-https://nutriplus.app.br}"

echo "==> API health (liveness)"
curl -sf "${API_URL}/actuator/health/liveness" | grep -q '"status":"UP"'

echo "==> Feature flags públicas"
curl -sf "${API_URL}/feature-flags" | grep -q 'REGISTRATION_OPEN'

echo "==> Planos públicos"
curl -sf "${API_URL}/plans" | grep -q 'billingEnabled'

echo "==> Forgot password (idempotency)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${API_URL}/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: smoke-$(date +%s)" \
  -d '{"email":"smoke@nutriplus.app.br"}')
test "$HTTP" = "200"

echo "==> Web landing"
curl -sf -o /dev/null "${WEB_URL}/"

echo "==> Web legal"
curl -sf -o /dev/null "${WEB_URL}/termos"
curl -sf -o /dev/null "${WEB_URL}/privacidade"

echo "OK — smoke básico passou."
