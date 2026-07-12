#!/usr/bin/env bash
# Gate de qualidade da API — unitários + integração + 18 cenários funcionais.
# Usado no GitHub Actions e pelo run-functional-tests.sh local.
set -euo pipefail

API_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$API_DIR"

if command -v mvn >/dev/null 2>&1; then
  MVN=mvn
elif [ -x "$API_DIR/mvnw" ]; then
  MVN="$API_DIR/mvnw"
else
  echo "ERROR: Maven não encontrado."
  exit 1
fi

UNIT_TEST_PACKAGES="**/service/**/*Test,**/controller/**/*Test,**/util/**/*Test,**/infrastructure/**/*Test,**/domain/**/*Test,**/application/**/*Test"
INTEGRATION_TEST_PACKAGES="**/integration/**/*Test"

echo "==> Nutri+ API functional gate"
echo "    Directory: $API_DIR"

echo ""
echo "==> [1/3] Unit tests"
$MVN -B test -Dtest="$UNIT_TEST_PACKAGES"

echo ""
echo "==> [2/3] Integration + functional scenarios (18 users + flows)"
$MVN -B test -Dtest="$INTEGRATION_TEST_PACKAGES" -Dsurefire.forkCount=1 -Dsurefire.reuseForks=true

echo ""
echo "==> [3/3] JaCoCo report"
$MVN -B jacoco:report

echo ""
echo "✅ API functional gate passed."
