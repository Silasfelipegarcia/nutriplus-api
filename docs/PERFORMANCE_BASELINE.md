# Performance Baseline — Nutri+ API

Gerado em 2026-06-28 20:30 UTC.

Comparativo de latência por ambiente. Gates k6 em `perf/k6/baseline.json`.

## Gates de referência

- Tier S p95 por endpoint: **200 ms**
- Fluxo dashboard agregado: **800 ms**
- Regressão máxima: **×1.2**

## Ambientes

### Local

- **Base URL:** `http://localhost:8080`
- **Audit em:** —
- **Tier S p95 agregado:** 54 ms
- **Fluxo dashboard (soma p95):** 185 ms
- **Falhas críticas:** 1


| Endpoint | Tier | p50 (ms) | p95 (ms) | SLA | Status |
|----------|------|----------|----------|-----|--------|
| `POST /auth/refresh` | A | 13 | 61 | 500 | OK |
| `GET /plans` | A | 7 | 18 | 500 | OK |
| `GET /app/bootstrap` | S | 9 | 16 | 200 | FAIL |
| `GET /care/my` | S | 8 | 12 | 200 | OK |
| `GET /checkins/adherence` | S | 17 | 23 | 200 | OK |
| `GET /checkins/stats` | S | 14 | 15 | 200 | OK |
| `GET /checkins/today` | S | 13 | 40 | 200 | OK |
| `GET /conversations` | S | 9 | 16 | 200 | OK |
| `GET /feature-flags` | S | 4 | 5 | 200 | OK |
| `GET /feedback/app/latest` | S | 10 | 20 | 200 | OK |
| `GET /health` | S | 2 | 2 | 200 | OK |
| `GET /legal/ai-disclosure` | S | 2 | 3 | 200 | OK |
| `GET /legal/data-sharing-consent` | S | 2 | 2 | 200 | OK |
| `GET /legal/privacy` | S | 2 | 2 | 200 | OK |
| `GET /legal/terms` | S | 2 | 12 | 200 | OK |
| `GET /meal-plans/generation-status` | S | 14 | 54 | 200 | OK |
| `GET /meal-plans/latest` | S | 16 | 25 | 200 | OK |
| `GET /nutrition-profile` | S | 30 | 30 | 200 | OK |
| `GET /nutritionists` | S | 9 | 13 | 200 | OK |
| `GET /pricing/guidelines` | S | 5 | 42 | 200 | OK |
| `GET /pro/dashboard` | S | 7 | 10 | 200 | OK |
| `GET /pro/patients` | S | 6 | 7 | 200 | OK |
| `GET /progress/evolution` | S | 12 | 20 | 200 | OK |
| `GET /progress/measurements/latest` | S | 11 | 12 | 200 | OK |
| `GET /progress/reviews/latest` | S | 7 | 7 | 200 | OK |
| `GET /progress/schedule` | S | 16 | 24 | 200 | OK |
| `GET /shopping-list/latest` | S | 17 | 40 | 200 | OK |
| `GET /training/profile` | S | 11 | 12 | 200 | OK |
| `GET /training/sports` | S | 2 | 16 | 200 | OK |
| `GET /users/me` | S | 10 | 11 | 200 | OK |

### Homolog

- **Base URL:** `https://nutriplus-api-production.up.railway.app`
- **Audit em:** 2026-06-28 20:30 UTC
- **Tier S p95 agregado:** 581 ms
- **Fluxo dashboard (soma p95):** 0 ms
- **Falhas críticas:** 0


| Endpoint | Tier | p50 (ms) | p95 (ms) | SLA | Status |
|----------|------|----------|----------|-----|--------|
| `POST /auth/refresh` | A | 547 | 549 | 500 | OK |
| `GET /plans` | A | 541 | 546 | 500 | SLOW |
| `GET /feature-flags` | S | 510 | 513 | 200 | SLOW |
| `GET /health` | S | 524 | 524 | 200 | SLOW |
| `GET /legal/ai-disclosure` | S | 517 | 539 | 200 | SLOW |
| `GET /legal/data-sharing-consent` | S | 531 | 538 | 200 | SLOW |
| `GET /legal/privacy` | S | 528 | 543 | 200 | SLOW |
| `GET /legal/terms` | S | 515 | 532 | 200 | SLOW |
| `GET /nutritionists` | S | 533 | 544 | 200 | SLOW |
| `GET /pricing/guidelines` | S | 508 | 530 | 200 | SLOW |
| `GET /training/sports` | S | 514 | 581 | 200 | SLOW |

### Prod

- **Base URL:** `https://nutriplus-api-production.up.railway.app`
- **Audit em:** —
- **Tier S p95 agregado:** 581 ms
- **Fluxo dashboard (soma p95):** 0 ms
- **Falhas críticas:** 0


| Endpoint | Tier | p50 (ms) | p95 (ms) | SLA | Status |
|----------|------|----------|----------|-----|--------|
| `POST /auth/refresh` | A | 547 | 549 | 500 | OK |
| `GET /plans` | A | 541 | 546 | 500 | SLOW |
| `GET /feature-flags` | S | 510 | 513 | 200 | SLOW |
| `GET /health` | S | 524 | 524 | 200 | SLOW |
| `GET /legal/ai-disclosure` | S | 517 | 539 | 200 | SLOW |
| `GET /legal/data-sharing-consent` | S | 531 | 538 | 200 | SLOW |
| `GET /legal/privacy` | S | 528 | 543 | 200 | SLOW |
| `GET /legal/terms` | S | 515 | 532 | 200 | SLOW |
| `GET /nutritionists` | S | 533 | 544 | 200 | SLOW |
| `GET /pricing/guidelines` | S | 508 | 530 | 200 | SLOW |
| `GET /training/sports` | S | 514 | 581 | 200 | SLOW |

## Como reproduzir

```bash
# Prod / homolog (read-only)
./perf/run-baseline.sh prod

# Local (API + MySQL)
BASE_URL=http://localhost:8080 ./perf/run-baseline.sh local
```
