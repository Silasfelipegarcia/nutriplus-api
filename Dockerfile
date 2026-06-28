# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS newrelic
ARG NEW_RELIC_AGENT_VERSION=9.3.0
RUN apk add --no-cache curl unzip \
    && curl -fsSL "https://download.newrelic.com/newrelic/java-agent/newrelic-agent/${NEW_RELIC_AGENT_VERSION}/newrelic-java-${NEW_RELIC_AGENT_VERSION}.zip" \
      -o /tmp/newrelic.zip \
    && unzip -q /tmp/newrelic.zip -d /opt \
    && rm /tmp/newrelic.zip

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=newrelic /opt/newrelic /opt/newrelic
COPY --from=build /app/target/*.jar app.jar
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
EXPOSE 8080
# Railway/prod: override via service variables. docker-compose sets local,dev explicitly.
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["/docker-entrypoint.sh"]
