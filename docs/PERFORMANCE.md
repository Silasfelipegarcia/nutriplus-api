# Performance — Nutri+ API

Matriz de endpoints, tiers de SLA, estratégia de cache e validação com k6.

## Tiers de SLA (p95)

| Tier | Alvo p95 | Uso |
|------|----------|-----|
| **S** | < 200 ms | GETs do portal (hot path) |
| **A** | < 500 ms | Auth e perfil leve |
| **B** | < 2000 ms | Writes com IA síncrona (macros) |
| **C** | < 30 s | Geração de plano (async + LLM) |

Baseline medido: [`PERFORMANCE_BASELINE.md`](PERFORMANCE_BASELINE.md).

## Matriz de endpoints (API)

Legenda: **cache** `none` | `http` | `app` | `client` · **k6** script em `perf/k6/` · **baseline** p95 local (ms, audit 2026-06-28)

| Endpoint | Método | Tier | Auth | Cache | k6 | Baseline local | Owner | Status |
|----------|--------|------|------|-------|-----|----------------|-------|--------|
| `/health` | GET | S | público | none | smoke | 2 | platform | OK |
| `/users/me` | GET | S | JWT | app | smoke | 11 | mobile | OK |
| `/app/bootstrap` | GET | S | JWT | composite | tier-s-full-dashboard | — | mobile | new |
| `/nutrition-profile` | GET | S | JWT | app | smoke | 30 | mobile | OK |
| `/nutrition-profile` | POST | B | JWT | — | tier-b-profile | — | mobile | OK |
| `/meal-plans/latest` | GET | S | JWT | app | smoke | 25 | mobile | OK |
| `/meal-plans/generation-status` | GET | S | JWT | none | tier-s-portal | 54 | mobile | OK |
| `/meal-plans/generate` | POST | C | JWT | — | generate-flow | — | mobile | OK |
| `/shopping-list/latest` | GET | S | JWT | app | smoke | 40 | mobile | OK |
| `/shopping-list/apply-swaps` | POST | B | JWT | evict | — | — | mobile | — |
| `/checkins/today` | GET | S | JWT | app | smoke | 40 | mobile | OK |
| `/checkins/stats` | GET | S | JWT | app | smoke | 15 | mobile | OK |
| `/checkins/adherence` | GET | S | JWT | app | — | 23 | mobile | OK |
| `/checkins` | POST | A | JWT | evict | — | — | mobile | — |
| `/checkins/extras` | POST | B | JWT | evict | — | — | mobile | — |
| `/progress/schedule` | GET | S | JWT | app | smoke | 24 | mobile | OK |
| `/progress/measurements/latest` | GET | S | JWT | app | — | 12 | mobile | OK |
| `/progress/evolution` | GET | S | JWT | client | — | 20 | mobile | OK |
| `/progress/reviews/latest` | GET | S | JWT | none | — | 7 | mobile | OK |
| `/progress/measurements` | POST | B | JWT | evict | — | — | mobile | — |
| `/progress/reviews` | POST | B | JWT | — | — | — | mobile | — |
| `/training/sports` | GET | S | público | app | smoke | 16 | mobile | OK |
| `/training/profile` | GET | S | JWT | client | — | 12 | mobile | OK |
| `/training/profile` | PUT | B | JWT | — | — | — | mobile | — |
| `/training/apply` | POST | C | JWT | — | — | — | mobile | — |
| `/onboarding/complete` | POST | B | JWT | evict | — | — | mobile | — |
| `/legal/*` | GET | S | público | app | smoke | 12 | web | OK |
| `/pricing/guidelines` | GET | S | público | http | tier-s-marketplace | 42 | marketplace | OK |
| `/nutritionists` | GET | S | público | none | tier-s-marketplace | 13 | marketplace | OK |
| `/nutritionists/{id}` | GET | S | público | none | tier-s-marketplace | — | marketplace | — |
| `/nutritionists/{id}/ratings` | GET | S | público | none | — | — | marketplace | — |
| `/care/my` | GET | S | JWT | none | — | 12 | care | OK |
| `/conversations` | GET | S | JWT | none | — | 16 | messaging | OK |
| `/conversations/{id}` | GET | S | JWT | none | — | — | messaging | paginated |
| `/conversations/{id}/messages` | POST | B | JWT | — | — | — | messaging | — |
| `/pro/dashboard` | GET | S | JWT pro | none | tier-s-pro-readonly | 10 | pro | OK |
| `/pro/patients` | GET | S | JWT pro | none | tier-s-pro-readonly | 7 | pro | OK |
| `/pro/patients/{id}/meal-plans` | GET | S | JWT pro | none | — | — | pro | batch N+1 fixed |
| `/pro/patients/{id}/dossier` | GET | S | JWT pro | none | — | — | pro | — |
| `/auth/login` | POST | A | público | none | tier-a-auth | — | auth | OK |
| `/auth/register` | POST | A | público | none | tier-a-auth | — | auth | OK |
| `/auth/refresh` | POST | A | público | none | — | 61 | auth | OK |
| `/feature-flags` | GET | S | público | none | — | 5 | platform | OK |
| `/plans` | GET | A | público | none | — | 18 | billing | OK |
| `/analytics/events` | POST | A | JWT | none | — | — | product | — |
| `/feedback/app/latest` | GET | S | JWT | none | — | 20 | product | OK |
| `/payments/*` | * | A/B | JWT | none | — | — | billing | ext I/O |
| `/admin/*` | * | B | JWT admin | none | — | — | admin | — |
| `/webhooks/*` | POST | — | provider | none | — | — | billing | no k6 |

## Agentes (nutriplus-agentes)

| Endpoint | Método | Tier | Cache | k6 | Notas |
|----------|--------|------|-------|-----|-------|
| `/health` | GET | B | none | agent-smoke | liveness |
| `/metrics` | GET | B | none | agent-smoke | Prometheus |
| `/api/v1/nutrition/calculate` | POST | B | none | — | determinístico |
| `/api/v1/meal-plan/generate` | POST | C | idempotency | generate-flow | LLM pipeline paralelo |
| `/api/v1/progress/analyze` | POST | C | idempotency | — | LLM |
| `/api/v1/food-extra/estimate` | POST | C | idempotency | — | LLM 8B candidato |
| `/api/v1/training/consult` | POST | C | idempotency | — | LLM |
| `/api/v1/substitutions/generate` | POST | C | idempotency | — | LLM |

## Cache no servidor

```bash
# Fase 1 — in-memory por instância (1 réplica Railway)
CACHE_ENABLED=true

# Fase 2 — multi-réplica
CACHE_ENABLED=true
spring.data.redis.host=<redis-host>
spring.data.redis.port=6379
```

TTLs (`nutriplus.cache.*`, defaults em `RedisCacheConfig`):

| Cache | TTL |
|-------|-----|
| `nutritionProfile` | 300 s |
| `mealPlanLatest` | 60 s |
| `shoppingListLatest` | 60 s |
| `checkinsToday` | 60 s |
| `checkinsStats` | 60 s |
| `progressSchedule` | 60 s |
| `progressMeasurementLatest` | 60 s |
| `userMe` | 60 s |
| `sportCatalog` / `legalDocuments` | 3600 s |

**Warm cache:** após `POST /auth/login` e `/auth/refresh`, `CacheWarmService` pré-carrega Tier S em background.

## k6 — scripts e baseline

```bash
# Local
./perf/k6/run-all.sh

# Homolog / prod (Tier S read-only; credenciais em PERF_TEST_EMAIL/PERF_TEST_PASSWORD)
BASE_URL=https://nutriplus-api-production.up.railway.app ./perf/k6/run-all.sh

# Audit 1-shot (multi-sample p50/p95)
python3 perf/audit-endpoints.py <BASE_URL> --env prod --skip-mutations

# Baseline completo
./perf/run-baseline.sh prod
```

| Script | Descrição |
|--------|-----------|
| `perf/k6/smoke.js` | Tier S — GETs públicos e autenticados |
| `perf/k6/tier-s-portal.js` | Fluxo portal parcial |
| `perf/k6/tier-s-full-dashboard.js` | 7 GETs sequenciais (abertura app) |
| `perf/k6/tier-s-marketplace.js` | Marketplace read-only |
| `perf/k6/tier-s-pro-readonly.js` | Pro dashboard/pacientes |
| `perf/k6/tier-a-auth.js` | Auth register/login |
| `perf/k6/tier-b-profile.js` | POST profile + IA |
| `perf/k6/generate-flow.js` | Tier C generate (local/homolog) |
| `perf/k6/agent-smoke.js` | Health/metrics agentes |
| `perf/k6/baseline.json` | Gates por ambiente (local/homolog/prod) |
| `perf/audit-endpoints.py` | Audit ~35 rotas com p50/p95 |

Gate nightly (`.github/workflows/k6-nightly.yml`): homolog default, preflight `/health`, falha se p95 Tier S ou dashboard flow > baseline × 1.2 ou `http_req_failed` > 5%. Secrets: `PERF_TEST_EMAIL`, `PERF_TEST_PASSWORD`.

## Otimizações implementadas

| Prioridade | Item | Status |
|------------|------|--------|
| P0 | `GET /app/bootstrap` bundle Tier S | done |
| P0 | Warm cache pós-login | done |
| P0 | TTL nutritionProfile 300s, checkins 60s | done |
| P1 | Batch ratings `/nutritionists` | done |
| P1 | Conversas sem mensagens na listagem + paginação GET `/{id}` | done |
| P1 | Batch meal-plans pro | done |
| P3 | Paralelo Flora+Helena, skip Mercado/Evandro LLM | done |
| P3 | Idempotency cache agentes (default true prod) | done |

## Métricas

- `nutriplus.http.server.duration` — timer por `flow` (header `X-Flow-Id`)
- SLO sync: 300 ms (`management.metrics.distribution.slo`)
- Agentes: `nutriplus_agent_llm_duration_seconds`

## O que não cachear

- Respostas de IA síncronas
- Polling de `generation-status` durante job ativo
- Webhooks e mutações idempotentes (replay via `Idempotency-Key`)
