#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
BASE_URL="${BASE_URL:-http://localhost:8080}"
OUT_DIR="${OUT_DIR:-$ROOT/perf/k6/results}"
K6_P95_MS="${K6_P95_MS:-200}"
mkdir -p "$OUT_DIR"
STAMP="$(date +%Y%m%d-%H%M%S)"

if ! command -v k6 >/dev/null 2>&1; then
  echo "k6 não encontrado. Instale: https://grafana.com/docs/k6/latest/set-up/install-k6/"
  exit 1
fi

echo "BASE_URL=$BASE_URL"
echo "OUT_DIR=$OUT_DIR"
echo "K6_P95_MS=$K6_P95_MS"

run() {
  local name="$1"
  local script="$2"
  echo "==> $name"
  k6 run --summary-export "$OUT_DIR/${name}-${STAMP}.json" \
    -e BASE_URL="$BASE_URL" \
    -e K6_P95_MS="$K6_P95_MS" \
    "$script"
}

run smoke "$ROOT/perf/k6/smoke.js"
run tier-s-portal "$ROOT/perf/k6/tier-s-portal.js"

echo "Resumos em $OUT_DIR"
