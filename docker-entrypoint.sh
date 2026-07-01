#!/bin/sh
set -e

JAVA_AGENT=""
if [ -n "${NEW_RELIC_LICENSE_KEY}" ]; then
  JAVA_AGENT="-javaagent:/opt/newrelic/newrelic.jar"
fi

exec java ${JAVA_AGENT} \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar app.jar --server.port="${PORT:-8080}"
