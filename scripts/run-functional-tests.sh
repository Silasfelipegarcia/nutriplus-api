#!/usr/bin/env bash
# Executa toda a suíte funcional automatizada do Nutri+ API (+ Flutter unit tests opcional).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
API_DIR="$ROOT/nutriplus-api"
FRONTEND_DIR="$ROOT/nutriplus-frontend"
AGENTES_DIR="$ROOT/nutriplus-agentes"

RUN_FLUTTER="${RUN_FLUTTER:-1}"
RUN_AGENTES="${RUN_AGENTES:-1}"
RUN_LOCAL_API_SMOKE="${RUN_LOCAL_API_SMOKE:-0}"
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"

echo "==> Nutri+ functional test suite"
echo "    API: $API_DIR"

# Docker local (Colima) — ignorado no GitHub Actions
if [ "${GITHUB_ACTIONS:-}" != "true" ] && [ "${CI:-}" != "true" ]; then
  if ! docker info >/dev/null 2>&1; then
    echo "==> Iniciando Colima (Docker)..."
    colima start --cpu 2 --memory 4 --disk 20 || true
  fi
  export DOCKER_HOST="${DOCKER_HOST:-unix://${HOME}/.colima/default/docker.sock}"
fi
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21 2>/dev/null || true)}"
export PATH="/opt/homebrew/bin:${PATH:-}"

echo "    Docker: ${DOCKER_HOST:-default}"
echo "    Java: ${JAVA_HOME:-system}"
echo "    Flutter tests: RUN_FLUTTER=$RUN_FLUTTER"
echo "    Agentes pytest: RUN_AGENTES=$RUN_AGENTES"
echo "    Local API smoke (dev users): RUN_LOCAL_API_SMOKE=$RUN_LOCAL_API_SMOKE"

chmod +x "$API_DIR/scripts/ci-functional-gate.sh"

echo ""
echo "==> [1/4] API functional gate"
"$API_DIR/scripts/ci-functional-gate.sh"

if [ "$RUN_AGENTES" = "1" ] && [ -d "$AGENTES_DIR" ]; then
  echo ""
  echo "==> [2/4] Agentes pytest"
  cd "$AGENTES_DIR"
  PYTEST_BIN=".venv/bin/pytest"
  if [ ! -x "$PYTEST_BIN" ]; then
    echo "    Instalando pytest no venv..."
    .venv/bin/pip install -q pytest
  fi
  USE_MOCK_LLM=true "$PYTEST_BIN" -q --tb=short
  cd "$API_DIR"
else
  echo ""
  echo "==> [2/4] Agentes pytest — skipped"
fi

if [ "$RUN_FLUTTER" = "1" ] && [ -d "$FRONTEND_DIR" ]; then
  echo ""
  echo "==> [3/4] Flutter analyze + unit tests"
  cd "$FRONTEND_DIR"
  flutter analyze --no-fatal-infos
  flutter test
  cd "$API_DIR"
else
  echo ""
  echo "==> [3/4] Flutter — skipped"
fi

if [ "$RUN_LOCAL_API_SMOKE" = "1" ]; then
  echo ""
  echo "==> [4/4] Local API dev-user smoke ($API_BASE_URL)"
  python3 "$API_DIR/scripts/functional_dev_users_validate.py" --base-url "$API_BASE_URL"
fi

echo ""
echo "✅ Functional test suite completed."
