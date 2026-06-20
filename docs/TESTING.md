# Testing — Nutri+ API

## Test pyramid

| Layer | Scope | Location | When |
|-------|--------|----------|------|
| **Unit** | Use cases, validators, JWT, mappers | `src/test/java/...` | Every PR |
| **Integration** | HTTP + MySQL (Testcontainers) | `src/test/java/.../integration` | Every PR |
| **Contract** | API ↔ agente IA (mocked agent in integration) | `MealPlanFlowIntegrationTest` | Every PR |
| **Performance** | k6 smoke | `perf/k6/smoke.js` | Manual / nightly |

Tests as Code (TaaC): CI runs `mvn verify` on every PR to `develop`, `homolog`, and `main`.

## Coverage gate

JaCoCo enforces **50% line coverage** minimum (`mvn verify`). Exclusions: `NutriplusApplication`, DTOs, entities, repositories, mappers, client, exception, legacy `security/**`, health/dev infra. Raise the threshold over time as coverage grows.

```bash
mvn verify
# report: target/site/jacoco/index.html
```

## Dev test user (local / dev profile)

With `SPRING_PROFILES_ACTIVE=local,dev`, the API seeds a ready-to-use account on first boot:

| Field | Value |
|-------|-------|
| Email | `teste@nutriplus.local` |
| Senha | `Nutri123!` |
| Persona | Luna (perfil nutricional completo) |

Use for manual testing of login → dashboard → gerar plano without re-onboarding.

## Local dev checklist (full stack)

Three services must run for meal-plan generation (Tier C):

| Step | Service | Command | URL |
|------|---------|---------|-----|
| 1 | MySQL 8 | local or Docker | `127.0.0.1:3306` |
| 2 | API | `mvn spring-boot:run` | `http://localhost:8080` |
| 3 | Agente | `uvicorn app.main:app --reload --port 8000` | `http://localhost:8000` |
| 4 | Flutter | `flutter run` | API base URL → `:8080` |

**Architecture:** Flutter → API → Agente. The Python agent does **not** call the API.

**404 on `/meal-plans/latest` or `/shopping-list/latest`** means the user has no plan yet — not a missing route. After a successful `POST /meal-plans/generate` job (`COMPLETED`), both endpoints return **200**.

Seed user (`local,dev`): `teste@nutriplus.local` / `Nutri123!` — full nutrition profile, ready to generate.

## Integration tests

Requires Docker (Testcontainers MySQL 8.4).

```bash
mvn test -Dtest='*IntegrationTest'
```

- `AbstractIntegrationTest` — `@SpringBootTest`, profile `test`, dynamic datasource
- `NutriplusApiIntegrationTest` — health, register → login → GET `/users/me`, PUT `/users/me`
- `MealPlanFlowIntegrationTest` — register → nutrition profile → generate (mocked `AiAgentClient`) → GET `/meal-plans/latest` **200**, GET `/shopping-list/latest` **200**

## Web slice tests

```bash
mvn test -Dtest='*ControllerTest'
```

`@WebMvcTest` with mocked services for `AuthController`, `HealthController`, `UserController`, and `MealPlanController` (`/generate`, `/generation-status`, `/latest`).

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

**Note:** `404` on `GET /meal-plans/latest` and `GET /shopping-list/latest` is **expected** for a new user with no plan — the endpoints exist; the API returns `ResourceNotFoundException`.

Thresholds: `http_req_duration` p(95) &lt; 200 ms for Tier S checks.

## k6 generate flow (Tier C — manual)

Requires **API + agente** running. Not run in CI by default (LLM latency).

```bash
BASE_URL=http://localhost:8080 \
K6_EMAIL=teste@nutriplus.local \
K6_PASSWORD=Nutri123! \
k6 run perf/k6/generate-flow.js
```

Polls `GET /meal-plans/generation-status` until `COMPLETED` or `FAILED`, then asserts `GET /meal-plans/latest` returns **200**.

## Spring Security testing

- Use `@WithMockUser` or real JWT from register/login in integration tests
- `@WebMvcTest` slices disable filters when testing controller mapping only
- `spring-security-test` on the classpath

## CI

`.github/workflows/ci.yml` — `mvn verify` (tests + JaCoCo gate), optional coverage artifact upload.
