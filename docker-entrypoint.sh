#!/bin/sh
set -e

JAVA_AGENT=""
if [ -n "${NEW_RELIC_LICENSE_KEY}" ]; then
  JAVA_AGENT="-javaagent:/opt/newrelic/newrelic.jar"
fi

exec java ${JAVA_AGENT} -jar app.jar --server.port="${PORT:-8080}"
