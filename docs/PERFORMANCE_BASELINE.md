# Performance Baseline — Nutri+ API

Gerado em 2026-07-01 21:52 UTC.

Comparativo de latência por ambiente. Gates k6 em `perf/k6/baseline.json`.

## Gates de referência

- Tier S p95 por endpoint: **200 ms**
- Fluxo dashboard agregado: **800 ms**
- Regressão máxima: **×1.2**

## Ambientes

### Local

- **Base URL:** `http://localhost:8080`
- **Audit em:** —
- **Autenticado:** não
- **Tier S p95 agregado:** 53 ms
- **Tier S warm p95:** — ms
- **Fluxo dashboard (soma p95):** 234 ms
- **Fluxo dashboard warm:** — ms
- **Falhas críticas:** 0


| Endpoint | Tier | cold (ms) | warm (ms) | p95 (ms) | SLA | Status |
|----------|------|-----------|-----------|----------|-----|--------|
| `POST /auth/refresh` | A | — | — | 80 | 500 | OK |
| `GET /plans` | A | — | — | 82 | 500 | OK |
| `GET /app/bootstrap` | S | — | — | 82 | 200 | OK |
| `GET /care/my` | S | — | — | 41 | 200 | OK |
| `GET /checkins/adherence` | S | — | — | 32 | 200 | OK |
| `GET /checkins/stats` | S | — | — | 32 | 200 | OK |
| `GET /checkins/today` | S | — | — | 31 | 200 | OK |
| `GET /conversations` | S | — | — | 41 | 200 | OK |
| `GET /feature-flags` | S | — | — | 6 | 200 | OK |
| `GET /feedback/app/latest` | S | — | — | 28 | 200 | OK |
| `GET /health` | S | — | — | 3 | 200 | OK |
| `GET /legal/ai-disclosure` | S | — | — | 8 | 200 | OK |
| `GET /legal/data-sharing-consent` | S | — | — | 5 | 200 | OK |
| `GET /legal/privacy` | S | — | — | 4 | 200 | OK |
| `GET /legal/terms` | S | — | — | 15 | 200 | OK |
| `GET /meal-plans/generation-status` | S | — | — | 26 | 200 | OK |
| `GET /meal-plans/latest` | S | — | — | 44 | 200 | OK |
| `GET /nutrition-profile` | S | — | — | 31 | 200 | OK |
| `GET /nutritionists` | S | — | — | 53 | 200 | OK |
| `GET /pricing/guidelines` | S | — | — | 25 | 200 | OK |
| `GET /pro/dashboard` | S | — | — | 16 | 200 | OK |
| `GET /pro/patients` | S | — | — | 26 | 200 | OK |
| `GET /progress/evolution` | S | — | — | 23 | 200 | OK |
| `GET /progress/measurements/latest` | S | — | — | 21 | 200 | OK |
| `GET /progress/reviews/latest` | S | — | — | 22 | 200 | OK |
| `GET /progress/schedule` | S | — | — | 34 | 200 | OK |
| `GET /shopping-list/latest` | S | — | — | 31 | 200 | OK |
| `GET /training/profile` | S | — | — | 26 | 200 | OK |
| `GET /training/sports` | S | — | — | 18 | 200 | OK |
| `GET /users/me` | S | — | — | 31 | 200 | OK |

### Homolog

- **Base URL:** `https://nutriplus-api-production.up.railway.app`
- **Audit em:** —
- **Autenticado:** não
- **Tier S p95 agregado:** 581 ms
- **Tier S warm p95:** — ms
- **Fluxo dashboard (soma p95):** 0 ms
- **Fluxo dashboard warm:** — ms
- **Falhas críticas:** 0


| Endpoint | Tier | cold (ms) | warm (ms) | p95 (ms) | SLA | Status |
|----------|------|-----------|-----------|----------|-----|--------|
| `POST /auth/refresh` | A | — | — | 549 | 500 | OK |
| `GET /plans` | A | — | — | 546 | 500 | SLOW |
| `GET /feature-flags` | S | — | — | 513 | 200 | SLOW |
| `GET /health` | S | — | — | 524 | 200 | SLOW |
| `GET /legal/ai-disclosure` | S | — | — | 539 | 200 | SLOW |
| `GET /legal/data-sharing-consent` | S | — | — | 538 | 200 | SLOW |
| `GET /legal/privacy` | S | — | — | 543 | 200 | SLOW |
| `GET /legal/terms` | S | — | — | 532 | 200 | SLOW |
| `GET /nutritionists` | S | — | — | 544 | 200 | SLOW |
| `GET /pricing/guidelines` | S | — | — | 530 | 200 | SLOW |
| `GET /training/sports` | S | — | — | 581 | 200 | SLOW |

### Prod

- **Base URL:** `https://nutriplus-api-production.up.railway.app`
- **Audit em:** 2026-07-01 21:52 UTC
- **Autenticado:** não
- **Warm-up /health (ms):** [1480, 2334, 535]
- **Tier S p95 agregado:** 5012 ms
- **Tier S warm p95:** 1781 ms
- **Fluxo dashboard (soma p95):** 0 ms
- **Fluxo dashboard warm:** 0 ms
- **Falhas críticas:** 0


| Endpoint | Tier | cold (ms) | warm (ms) | p95 (ms) | SLA | Status |
|----------|------|-----------|-----------|----------|-----|--------|
| `POST /auth/refresh` | A | 505 | 604 | 717 | 500 | OK |
| `GET /plans` | A | 551 | 556 | 623 | 500 | SLOW |
| `GET /feature-flags` | S | 781 | 609 | 876 | 200 | SLOW |
| `GET /health` | S | 507 | 1221 | 2410 | 200 | SLOW |
| `GET /legal/ai-disclosure` | S | 824 | 946 | 1379 | 200 | SLOW |
| `GET /legal/data-sharing-consent` | S | 538 | 1178 | 3009 | 200 | SLOW |
| `GET /legal/health-eligibility` | S | 527 | 661 | 1005 | 200 | SLOW |
| `GET /legal/nutritionist-terms` | S | 511 | 560 | 605 | 200 | SLOW |
| `GET /legal/privacy` | S | 673 | 1435 | 4196 | 200 | SLOW |
| `GET /legal/terms` | S | 1876 | 1068 | 2550 | 200 | SLOW |
| `GET /nutritionists` | S | 666 | 546 | 666 | 200 | SLOW |
| `GET /pricing/guidelines` | S | 536 | 1781 | 5012 | 200 | SLOW |
| `GET /training/sports` | S | 563 | 1355 | 2057 | 200 | SLOW |

## Como reproduzir

```bash
# Prod / homolog (read-only)
./perf/run-baseline.sh prod

# Local (API + MySQL)
BASE_URL=http://localhost:8080 ./perf/run-baseline.sh local
```
