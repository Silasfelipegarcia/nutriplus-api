#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV="${1:-prod}"
BASE_URL="${BASE_URL:-https://nutriplus-api-production.up.railway.app}"
SAMPLES="${SAMPLES:-3}"
SKIP_MUTATIONS="${SKIP_MUTATIONS:-true}"

if [[ "$ENV" == "local" ]]; then
  BASE_URL="${BASE_URL:-http://localhost:8080}"
elif [[ "$ENV" == "homolog" ]]; then
  BASE_URL="${BASE_URL:-https://nutriplus-api-production.up.railway.app}"
fi

OUT="$ROOT/perf/results/audit-${ENV}-$(date +%Y%m%d-%H%M%S).json"
mkdir -p "$ROOT/perf/results" "$ROOT/perf/k6/results"

ARGS=(python3 "$ROOT/perf/audit-endpoints.py" "$BASE_URL" --env "$ENV" --samples "$SAMPLES" --out "$OUT")
if [[ "$SKIP_MUTATIONS" == "true" ]]; then
  ARGS+=(--skip-mutations)
fi

echo "==> Audit $ENV ($BASE_URL)"
"${ARGS[@]}"

if command -v k6 >/dev/null 2>&1; then
  echo "==> k6 Tier S ($ENV)"
  BASE_URL="$BASE_URL" "$ROOT/perf/k6/run-all.sh" || true
else
  echo "k6 não instalado — pulando load tests"
fi

echo "==> Baseline report"
python3 "$ROOT/perf/generate-baseline-report.py" --env "$ENV" --audit "$OUT"
