# NRQL Cookbook — Nutri+ API

Queries prontas para investigar erros, latência e traces no [New Relic](https://one.newrelic.com).

Substitua `nutriplus-api-prod` pelo valor de `NEW_RELIC_APP_NAME` do ambiente (ex.: `nutriplus-api-homolog`).

Complementa [`NEW_RELIC.md`](./NEW_RELIC.md) e [`../OBSERVABILITY.md`](../OBSERVABILITY.md).

---

## Checklist pós-deploy (validar ingestão)

Após deploy com `NEW_RELIC_LICENSE_KEY` + [`newrelic.yml`](../../newrelic.yml):

1. **APM → Services** → app aparece (`nutriplus-api-prod`) com dados nos últimos 5 min
2. **Distributed tracing** → abrir transaction `WebTransaction/SpringController/...`
3. **Logs** → query abaixo retorna linhas com `entity.name = 'nutriplus-api-prod'`
4. **Custom attributes** → na transaction, aba Attributes: `correlationId`, `traceId`, `flowId`
5. **Railway env vars:**
   - `NEW_RELIC_LICENSE_KEY`
   - `NEW_RELIC_APP_NAME=nutriplus-api-prod`
   - `NEW_RELIC_ENVIRONMENT=production`

Teste rápido: faça um request autenticado com header `X-Correlation-Id: nr-smoke-test-001` e busque no NR:

```sql
SELECT * FROM Transaction
WHERE appName = 'nutriplus-api-prod'
  AND correlationId = 'nr-smoke-test-001'
SINCE 15 minutes ago
```

---

## Por correlationId (suporte / usuário)

Quando o app ou portal devolve `correlationId` no JSON de erro:

```sql
SELECT timestamp, message, correlationId, traceId, flowId, userId, httpPath, httpStatus
FROM Log
WHERE entity.name = 'nutriplus-api-prod'
  AND correlationId = 'COLE-AQUI-O-UUID'
SINCE 24 hours ago
LIMIT 100
```

```sql
SELECT name, duration, error, correlationId, traceId, flowId, userId, httpPath, httpStatus
FROM Transaction
WHERE appName = 'nutriplus-api-prod'
  AND correlationId = 'COLE-AQUI-O-UUID'
SINCE 24 hours ago
```

Cross-service (API + agente):

```sql
SELECT * FROM Span
WHERE correlationId = 'COLE-AQUI-O-UUID'
SINCE 24 hours ago
LIMIT 200
```

---

## Erros recentes

```sql
SELECT count(*) FROM Transaction
WHERE appName = 'nutriplus-api-prod' AND error IS true
FACET name
SINCE 1 hour ago
```

```sql
SELECT timestamp, message, correlationId, userId, httpPath
FROM Log
WHERE entity.name = 'nutriplus-api-prod'
  AND (message LIKE '%UNHANDLED%' OR message LIKE '%AI_AGENT%' OR level = 'ERROR')
SINCE 1 hour ago
LIMIT 50
```

```sql
SELECT timestamp, message, correlationId, httpPath, httpStatus
FROM Log
WHERE entity.name = 'nutriplus-api-prod'
  AND message LIKE '%client-error%'
SINCE 1 hour ago
LIMIT 50
```

---

## Latência por endpoint

```sql
SELECT percentile(duration, 50, 95, 99)
FROM Transaction
WHERE appName = 'nutriplus-api-prod'
FACET name
SINCE 1 day ago
```

Sync flows (SLO 300 ms):

```sql
SELECT percentile(duration, 95)
FROM Transaction
WHERE appName = 'nutriplus-api-prod'
  AND flowId IS NOT NULL
FACET flowId
SINCE 1 day ago
```

Geração de plano:

```sql
SELECT *
FROM Transaction
WHERE appName = 'nutriplus-api-prod'
  AND flowId = 'generate-meal-plan'
SINCE 6 hours ago
LIMIT 20
```

---

## Distributed tracing (API → agente)

```sql
SELECT * FROM Span
WHERE appName IN ('nutriplus-api-prod', 'nutriplus-agentes-prod')
  AND traceId = 'COLE-AQUI-O-TRACE-ID'
SINCE 24 hours ago
```

Ou abra **APM → Distributed tracing** e filtre por transaction lenta em `POST /meal-plans/generate` (ou equivalente).

---

## JDBC / MySQL (queries lentas)

Spans JDBC automáticos do Java agent:

```sql
SELECT average(duration), count(*)
FROM Span
WHERE appName = 'nutriplus-api-prod'
  AND db.system = 'mysql'
FACET db.statement
SINCE 1 hour ago
LIMIT 20
```

### MySQL Database Monitoring (servidor)

Se integração configurada no painel (`nutriplus-mysql-prod`):

- **Databases → nutriplus-mysql-prod** → Overview, Query performance, Buffer pool
- NRQL exemplo:

```sql
SELECT average(`mysql.innodb.bufferPoolHitRate`)
FROM Metric
WHERE entity.name = 'nutriplus-mysql-prod'
SINCE 1 hour ago
TIMESERIES
```

Setup do usuário `newrelic` + integração: [`NEW_RELIC.md` — MySQL](./NEW_RELIC.md#mysql--duas-camadas).

---

## Sessão do app

```sql
SELECT timestamp, message, correlationId, flowId, userId
FROM Log
WHERE entity.name = 'nutriplus-api-prod'
  AND sessionId = 'UUID-DA-SESSAO'
SINCE 24 hours ago
LIMIT 100
```

---

## Fallback (sem custom attribute ainda)

Se deploy anterior sem `NewRelicTraceBridge`:

```sql
SELECT timestamp, message
FROM Log
WHERE entity.name = 'nutriplus-api-prod'
  AND message LIKE '%cid=COLE-PARTE-DO-UUID%'
SINCE 24 hours ago
```

Ou grep nos logs Railway + SQL:

```sql
SELECT * FROM ai_requests_log WHERE correlation_id = '<cid>' ORDER BY created_at DESC;
SELECT * FROM audit_log WHERE correlation_id = '<cid>' ORDER BY created_at DESC;
```
