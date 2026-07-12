# Endpoints por Tier (resumo)

## Tier S (hot path)

`GET /app/bootstrap`, `/users/me`, `/meal-plans/latest`, `/checkins/today`, `/checkins/stats`, `/nutrition-profile`, `/pro/dashboard`, `/pro/patients`, `/legal/*`, `/feature-flags`

Estratégia: `@Cacheable` app-level ou HTTP público; índices em `user_id`.

## Tier A

`POST /auth/login`, `/auth/register`, `POST /checkins`

## Tier B

`POST /nutrition-profile`, `/checkins/extras`, `/progress/measurements`, `/conversations/{id}/messages`, `/shopping-list/apply-swaps`

## Tier C

`POST /meal-plans/generate`, `/training/apply` — async job, não bloquear thread

## k6

Scripts em `perf/k6/` — `smoke`, `tier-s-full-dashboard`, `generate-flow`

## Railway

Tuning prod: `RAILWAY_PERFORMANCE.md`, `docker-entrypoint.sh` heap/GC — não remover sem revisão.
