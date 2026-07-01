#!/bin/sh
set -e

JAVA_AGENT=""
if [ -n "${NEW_RELIC_LICENSE_KEY}" ]; then
  JAVA_AGENT="-javaagent:/opt/newrelic/newrelic.jar"
fi

# Defaults tuned for Railway single-replica prod (override via JAVA_OPTS env).
JAVA_OPTS="${JAVA_OPTS:--Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication}"

exec java ${JAVA_AGENT} ${JAVA_OPTS} -jar app.jar --server.port="${PORT:-8080}"
