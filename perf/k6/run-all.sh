#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
BASE_URL="${BASE_URL:-http://localhost:8080}"
AGENT_URL="${AGENT_URL:-http://localhost:8000}"
OUT_DIR="${OUT_DIR:-$ROOT/perf/k6/results}"
K6_P95_MS="${K6_P95_MS:-200}"
K6_TIER_S_P95_MS="${K6_TIER_S_P95_MS:-200}"
mkdir -p "$OUT_DIR"
STAMP="$(date +%Y%m%d-%H%M%S)"

if ! command -v k6 >/dev/null 2>&1; then
  echo "k6 não encontrado. Instale: https://grafana.com/docs/k6/latest/set-up/install-k6/"
  exit 1
fi

echo "BASE_URL=$BASE_URL"
echo "AGENT_URL=$AGENT_URL"
echo "OUT_DIR=$OUT_DIR"
echo "K6_P95_MS=$K6_P95_MS"

run() {
  local name="$1"
  local script="$2"
  echo "==> $name"
  k6 run --summary-export "$OUT_DIR/${name}-${STAMP}.json" \
    -e BASE_URL="$BASE_URL" \
    -e AGENT_URL="$AGENT_URL" \
    -e K6_P95_MS="$K6_P95_MS" \
    -e K6_TIER_S_P95_MS="$K6_TIER_S_P95_MS" \
    "$script"
}

run smoke "$ROOT/perf/k6/smoke.js"
run tier-s-portal "$ROOT/perf/k6/tier-s-portal.js"
run tier-s-full-dashboard "$ROOT/perf/k6/tier-s-full-dashboard.js"
run tier-s-marketplace "$ROOT/perf/k6/tier-s-marketplace.js"
run tier-s-pro-readonly "$ROOT/perf/k6/tier-s-pro-readonly.js"

if curl -sf "$AGENT_URL/health" >/dev/null 2>&1; then
  run agent-smoke "$ROOT/perf/k6/agent-smoke.js"
else
  echo "==> agent-smoke (skipped — agentes indisponível em $AGENT_URL)"
fi

echo "Resumos em $OUT_DIR"
