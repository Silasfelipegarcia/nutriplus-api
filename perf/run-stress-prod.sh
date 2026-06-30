#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BASE_URL="${BASE_URL:-https://nutriplus-api-production.up.railway.app}"
MAX_VUS="${STRESS_MAX_VUS:-10}"
K6_P95_MS="${K6_P95_MS:-800}"
STAMP="$(date +%Y%m%d-%H%M%S)"
OUT_DIR="$ROOT/perf/k6/results"
SUMMARY="$OUT_DIR/stress-prod-${STAMP}.json"

mkdir -p "$OUT_DIR"

if ! command -v k6 >/dev/null 2>&1; then
  echo "Instale k6: https://grafana.com/docs/k6/latest/set-up/install-k6/"
  exit 1
fi

echo "==> Stress test prod"
echo "    BASE_URL=$BASE_URL"
echo "    STRESS_MAX_VUS=$MAX_VUS"
echo "    K6_P95_MS=$K6_P95_MS"
is_remote=0
if [[ "$BASE_URL" != *localhost* && "$BASE_URL" != *127.0.0.1* ]]; then
  is_remote=1
fi

if [[ -z "${PERF_TEST_EMAIL:-}" || -z "${PERF_TEST_PASSWORD:-}" ]]; then
  if [[ "$is_remote" -eq 1 ]]; then
    echo "ERRO: prod/homolog exige PERF_TEST_EMAIL e PERF_TEST_PASSWORD."
    echo "      Cadastro em prod está fechado — register efêmero não funciona."
    echo ""
    echo "  export PERF_TEST_EMAIL='...'"
    echo "  export PERF_TEST_PASSWORD='...'"
    echo "  ./perf/run-stress-prod.sh"
    exit 1
  fi
  echo "    PERF_TEST_EMAIL=(local default)"
else
  echo "    PERF_TEST_EMAIL=$PERF_TEST_EMAIL"
fi
echo ""

# Preflight
code="$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/health")"
if [[ "$code" != "200" ]]; then
  echo "API indisponível ($code)"
  exit 1
fi

# Register só em local; prod rejeita novos cadastros
perf_register="${PERF_REGISTER:-0}"

k6 run \
  -e BASE_URL="$BASE_URL" \
  -e STRESS_MAX_VUS="$MAX_VUS" \
  -e K6_P95_MS="$K6_P95_MS" \
  -e PERF_REGISTER="$perf_register" \
  -e PERF_TEST_EMAIL="${PERF_TEST_EMAIL:-}" \
  -e PERF_TEST_PASSWORD="${PERF_TEST_PASSWORD:-}" \
  -e STRESS_SUMMARY_OUT="$SUMMARY" \
  "$ROOT/perf/k6/stress-prod.js"

echo ""
echo "Resumo JSON: $SUMMARY"
