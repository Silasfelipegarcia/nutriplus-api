#!/usr/bin/env bash
# Reparo Flyway no MySQL local (IntelliJ).
# - checksum mismatch V56 → flyway:repair
# - V1 failed / histórico corrompido (type DELETE) → baseline + migrate
#
# Uso: ./scripts/local-dev-flyway-repair.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

URL="${DB_URL:-jdbc:mysql://127.0.0.1:3306/nutriplus?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci}"
USER="${DB_USERNAME:-root}"
PASS="${DB_PASSWORD:-}"
BASELINE_VERSION="${FLYWAY_BASELINE_VERSION:-58}"

MVN_FLYWAY=(mvn -q flyway:repair
  -Dflyway.url="$URL"
  -Dflyway.user="$USER"
  -Dflyway.password="$PASS")

MVN_VALIDATE=(mvn -q flyway:validate
  -Dflyway.url="$URL"
  -Dflyway.user="$USER"
  -Dflyway.password="$PASS")

mysql_query() {
  mysql -h127.0.0.1 -u"$USER" ${PASS:+-p"$PASS"} nutriplus -N -e "$1" 2>/dev/null || echo ""
}

needs_baseline_reset() {
  local failed delete_rows users_exists
  failed="$(mysql_query "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='nutriplus' AND table_name='flyway_schema_history';")"
  [[ "$failed" == "0" ]] && return 1

  failed="$(mysql_query "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 0;")"
  delete_rows="$(mysql_query "SELECT COUNT(*) FROM flyway_schema_history WHERE type = 'DELETE';")"
  users_exists="$(mysql_query "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='nutriplus' AND table_name='users';")"

  [[ "${failed:-0}" -gt 0 ]] && return 0
  [[ "${delete_rows:-0}" -gt 0 && "${users_exists:-0}" -gt 0 ]] && return 0
  return 1
}

run_baseline_reset() {
  echo "→ histórico Flyway corrompido — baseline em V${BASELINE_VERSION} + migrate"
  mysql_query "DROP TABLE IF EXISTS flyway_schema_history;" >/dev/null || true
  mvn -q flyway:baseline \
    -Dflyway.url="$URL" \
    -Dflyway.user="$USER" \
    -Dflyway.password="$PASS" \
    -Dflyway.baselineVersion="$BASELINE_VERSION" \
    -Dflyway.baselineDescription="local schema repair"
  mvn -q flyway:migrate \
    -Dflyway.url="$URL" \
    -Dflyway.user="$USER" \
    -Dflyway.password="$PASS"
}

echo "→ mvn clean (remove target/ com migrations duplicadas)"
mvn clean -q

if needs_baseline_reset; then
  run_baseline_reset
else
  echo "→ flyway:repair (checksum / entradas falhas)"
  "${MVN_FLYWAY[@]}"
fi

echo "→ flyway:validate"
if ! "${MVN_VALIDATE[@]}"; then
  echo "→ validate falhou — tentando baseline reset"
  run_baseline_reset
  "${MVN_VALIDATE[@]}"
fi

echo ""
echo "OK — pode subir NutriplusApplication no IntelliJ (profile local)."
