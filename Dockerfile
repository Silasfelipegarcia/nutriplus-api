# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Railway/prod: override via service variables. docker-compose sets local,dev explicitly.
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["sh", "-c", "exec java -jar app.jar --server.port=${PORT:-8080}"]
