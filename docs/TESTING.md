# Testing — Nutri+ API

## Test pyramid

| Layer | Scope | Location | When |
|-------|--------|----------|------|
| **Unit** | Use cases, validators, JWT, mappers | `src/test/java/...` | Every PR |
| **Integration** | HTTP + MySQL (Testcontainers) | `src/test/java/.../integration` | Every PR |
| **Contract** | API ↔ agente IA (future) | TBD | Homolog |
| **Performance** | k6 smoke | `perf/k6/smoke.js` | Manual / nightly |

Tests as Code (TaaC): CI runs `mvn verify` on every PR to `develop`, `homolog`, and `main`.

## Coverage gate

JaCoCo enforces **50% line coverage** minimum (`mvn verify`). Exclusions: `NutriplusApplication`, DTO records. Raise the threshold over time as coverage grows.

```bash
mvn verify
# report: target/site/jacoco/index.html
```

## Integration tests

Requires Docker (Testcontainers MySQL 8.4).

```bash
mvn test -Dtest='*IntegrationTest'
```

- `AbstractIntegrationTest` — `@SpringBootTest`, profile `test`, dynamic datasource
- `AuthIntegrationTest` — register → login → GET `/users/me`
- `HealthIntegrationTest` — GET `/health`
- `UserControllerIntegrationTest` — PUT `/users/me` with JWT

## Web slice tests

```bash
mvn test -Dtest='*ControllerTest'
```

`@WebMvcTest` with mocked services for `AuthController` and `HealthController`.

## SLA tiers (realistic targets)

| Tier | p95 target | Endpoints |
|------|------------|-----------|
| **S** | &lt; 200 ms | `GET /health`, `GET /users/me`, `GET /nutrition-profile`, `GET /meal-plans/latest`, `GET /shopping-list/latest` |
| **A** | &lt; 500 ms | `POST /auth/login`, `POST /auth/register`, `POST /auth/refresh`, `PUT /users/me` |
| **B** | &lt; 2000 ms | `POST /nutrition-profile` |
| **C** | &lt; 30 s (LLM) | `POST /meal-plans/generate` — async job planned for production scale |

Tier S/A/B are validated locally or in homolog with k6; Tier C is not held to 200 ms.

## k6 smoke (Tier S)

Install [k6](https://k6.io/docs/get-started/installation/), start the API, then:

```bash
# Local
BASE_URL=http://localhost:8080 k6 run perf/k6/smoke.js

# Homolog
BASE_URL=https://api-homolog.example.com k6 run perf/k6/smoke.js
```

Optional: `K6_EMAIL` / `K6_PASSWORD` for an existing user; otherwise the script registers a ephemeral user.

Thresholds: `http_req_duration` p(95) &lt; 200 ms for Tier S checks.

## Spring Security testing

- Use `@WithMockUser` or real JWT from register/login in integration tests
- `@WebMvcTest` slices disable filters when testing controller mapping only
- `spring-security-test` on the classpath

## CI

`.github/workflows/ci.yml` — `mvn verify` (tests + JaCoCo gate), optional coverage artifact upload.
