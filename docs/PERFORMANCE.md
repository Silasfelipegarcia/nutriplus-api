# Performance — Nutri+ API

Matriz de endpoints, tiers de SLA, estratégia de cache e validação com k6.

## Tiers de SLA (p95)

| Tier | Alvo p95 | Uso |
|------|----------|-----|
| **S** | < 200 ms | GETs do portal (hot path) |
| **A** | < 500 ms | Auth e perfil leve |
| **B** | < 2000 ms | Writes com IA síncrona (macros) |
| **C** | < 30 s | Geração de plano (async + LLM) |

## Matriz de endpoints

| Endpoint | Método | Tier | Tipo | Auth | Cache | Invalidar em |
|----------|--------|------|------|------|-------|--------------|
| `/health` | GET | S | read | público | none | — |
| `/users/me` | GET | S | read | JWT | client | PUT `/users/me` |
| `/nutrition-profile` | GET | S | read | JWT | app + client | POST profile, onboarding, treino apply |
| `/nutrition-profile` | POST | B | write+AI | JWT | — | evict nutrition |
| `/onboarding/complete` | POST | B | write+AI | JWT | — | evict nutrition, checkins |
| `/meal-plans/latest` | GET | S | read | JWT | app | generate completed |
| `/meal-plans/generation-status` | GET | S | read | JWT | none (poll) | — |
| `/meal-plans/generate` | POST | C | write+async | JWT | — | evict meal plan, shopping |
| `/shopping-list/latest` | GET | S | read | JWT | app | generate, apply-swaps |
| `/checkins/today` | GET | S | read | JWT | app | POST checkin |
| `/checkins/stats` | GET | S | read | JWT | app | POST checkin |
| `/checkins` | POST | A | write | JWT | — | evict checkins |
| `/progress/schedule` | GET | S | read | JWT | app | POST measurement |
| `/progress/measurements/latest` | GET | S | read | JWT | app | POST measurement |
| `/progress/evolution` | GET | S | read | JWT | client | POST measurement, review |
| `/training/sports` | GET | S | read | público | http + client | — |
| `/training/profile` | GET | S | read | JWT | client | PUT profile, apply |
| `/legal/*` | GET | S | read | público | http | — |
| `/pricing/guidelines` | GET | S | read | público | http | — |
| `/auth/login` | POST | A | write | público | none | — |
| `/auth/register` | POST | A | write | público | none | — |
| `/auth/refresh` | POST | A | write | público | none | — |

**Legenda cache:** `none` | `http` (Cache-Control CDN/browser) | `app` (`@Cacheable`, flag `CACHE_ENABLED`) | `client` (PortalDataStore no web).

## Cache no servidor

```bash
# Fase 1 — in-memory por instância (1 réplica Railway)
CACHE_ENABLED=true

# Fase 2 — multi-réplica
CACHE_ENABLED=true
spring.data.redis.host=<redis-host>
spring.data.redis.port=6379
```

TTLs padrão (`nutriplus.cache.*`):

| Cache | TTL |
|-------|-----|
| `nutritionProfile` | 120 s |
| `mealPlanLatest` | 60 s |
| `shoppingListLatest` | 60 s |
| `checkinsToday` | 30 s |
| `checkinsStats` | 30 s |
| `progressSchedule` | 60 s |
| `progressMeasurementLatest` | 60 s |

## k6 — scripts e baseline

```bash
# Local (API rodando em :8080)
./perf/k6/run-all.sh

# Homolog / prod (somente leitura Tier S)
BASE_URL=https://nutriplus-api-production.up.railway.app ./perf/k6/run-all.sh
```

| Script | Descrição |
|--------|-----------|
| `perf/k6/smoke.js` | Tier S — GETs públicos e autenticados |
| `perf/k6/tier-s-portal.js` | Fluxo “abrir dashboard” após login |
| `perf/k6/baseline.json` | Referência p95 Tier S para gate de regressão |

Gate de regressão: falha se p95 Tier S > baseline × 1.2 (ver `.github/workflows/k6-nightly.yml`).

## Métricas

- `nutriplus.http.server.duration` — timer por `flow` (header `X-Flow-Id`)
- SLO sync: 300 ms (`management.metrics.distribution.slo`)
- Grafana: `docs/observability/grafana-dashboard-operations.json`

## O que não cachear

- Respostas de IA síncronas (`POST /nutrition-profile`, `/progress/reviews`)
- Polling de `generation-status` durante job ativo
- Webhooks e mutações idempotentes (replay via `Idempotency-Key`)
