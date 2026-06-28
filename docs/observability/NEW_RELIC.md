# New Relic — Nutri+

Guia de configuração do New Relic para API, agente Python e MySQL no Railway.

Complementa [`../OBSERVABILITY.md`](../OBSERVABILITY.md). Prometheus/Grafana **permanecem ativos** — NR é camada adicional.

---

## Pré-requisitos

1. Conta New Relic compartilhada com **lupa-cnpj-api** (mesma license key).
2. **License Key** em [one.newrelic.com](https://one.newrelic.com) → API Keys → License key.
3. Serviços deployados no Railway com variáveis configuradas.

---

## APM — nutriplus-api (Java)

Mesmo padrão do `lupa-cnpj-api`: multi-stage Dockerfile + [`docker-entrypoint.sh`](../../docker-entrypoint.sh). O agent (v9.3.0) só anexa ao JVM quando `NEW_RELIC_LICENSE_KEY` está definida.

| Variável | Descrição |
|----------|-----------|
| `NEW_RELIC_LICENSE_KEY` | License key (mesma do Lupa CNPJ) |
| `NEW_RELIC_APP_NAME` | Nome no painel NR |
| `NEW_RELIC_ENVIRONMENT` | Opcional — `development` / `staging` / `production` |

| Ambiente Railway | `NEW_RELIC_APP_NAME` | `NEW_RELIC_ENVIRONMENT` |
|------------------|----------------------|-------------------------|
| develop | `nutriplus-api-dev` | `development` |
| homolog | `nutriplus-api-homolog` | `staging` |
| prod | `nutriplus-api-prod` | `production` |

**Instrumentação automática:** Spring MVC, JDBC/HikariCP (queries MySQL), HTTP client (`AiAgentClient`), JVM metrics, log forwarding (stdout JSON em prod).

---

## APM — nutriplus-agentes (Python)

Mesma license key do `lupa-cnpj-api`. O agent envolve o Uvicorn via `newrelic-admin run-program` quando a license key está presente.

| Variável | Descrição |
|----------|-----------|
| `NEW_RELIC_LICENSE_KEY` | License key (mesma do Lupa CNPJ) |
| `NEW_RELIC_APP_NAME` | Nome no painel NR |
| `NEW_RELIC_ENVIRONMENT` | Opcional — `development` / `staging` / `production` |

| Ambiente Railway | `NEW_RELIC_APP_NAME` | `NEW_RELIC_ENVIRONMENT` |
|------------------|----------------------|-------------------------|
| develop | `nutriplus-agentes-dev` | `development` |
| homolog | `nutriplus-agentes-homolog` | `staging` |
| prod | `nutriplus-agentes-prod` | `production` |

**Custom attributes** por request (via `TraceMiddleware`): `correlationId`, `traceId`, `flowId`, `sessionId`, `idempotencyKey`.

---

## MySQL — duas camadas

### Camada A — JDBC (automática)

O Java agent na API captura tempo de queries, throughput e erros SQL por endpoint. Não requer setup extra.

### Camada B — Database Monitoring (servidor MySQL)

Métricas do servidor: buffer pool, conexões, slow queries, `performance_schema`.

#### 1. Criar usuário de monitoramento

Conecte ao MySQL do Railway (plugin MySQL → Connect) e execute **por ambiente** (dev, homolog, prod têm instâncias separadas):

```sql
CREATE USER 'newrelic'@'%' IDENTIFIED BY '<senha-forte>';
GRANT REPLICATION CLIENT ON *.* TO 'newrelic'@'%';
GRANT SELECT ON performance_schema.* TO 'newrelic'@'%';
FLUSH PRIVILEGES;
```

#### 2. Configurar no painel New Relic

1. **Add data** → **MySQL**.
2. Informe host, porta, database, usuário `newrelic` e senha.
3. Use as variáveis do plugin Railway MySQL: `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`.
4. Repita para cada ambiente.

> **Railway:** não é possível instalar `nri-mysql` no host gerenciado. A integração remota via credenciais no painel NR é o caminho correto.

#### 3. Nomear instâncias no NR

| Ambiente | Nome sugerido |
|----------|---------------|
| develop | `nutriplus-mysql-dev` |
| homolog | `nutriplus-mysql-homolog` |
| prod | `nutriplus-mysql-prod` |

---

## Correlação cross-service

Busque no NR Logs ou Distributed Tracing usando os mesmos IDs do contrato HTTP:

| Atributo | Origem |
|----------|--------|
| `correlationId` | Header `X-Correlation-Id` (Flutter → API → agente) |
| `traceId` | Header `X-Trace-Id` |
| `flowId` | Header `X-Flow-Id` |
| `sessionId` | Header `X-Session-Id` |

No mobile, crashes no Firebase Crashlytics incluem custom key `app_env` e `session_id` para correlacionar com logs da API.

---

## Alertas sugeridos (espelhar SLOs existentes)

| Alerta | Condição | Referência |
|--------|----------|------------|
| Sync lento | p95 `nutriplus.http.server.duration` (sync=true) > 300ms | `application.properties` SLO |
| Agente IA lento | p95 chamadas ao agente > 30s | `prometheus-alerts.yml` |
| Erro rate API | Taxa de 5xx > threshold | Dashboard operations |
| MySQL conexões | Conexões ativas > 80% do pool | DB Monitoring |

---

## Checklist manual (time)

- [ ] Copiar `NEW_RELIC_LICENSE_KEY` do Railway do **lupa-cnpj-api** (mesma conta)
- [ ] `NEW_RELIC_LICENSE_KEY` + `NEW_RELIC_APP_NAME` + `NEW_RELIC_ENVIRONMENT` em cada service Railway (API + agente × 3 ambientes)
- [ ] Usuário `newrelic` criado no MySQL de cada ambiente
- [ ] Integração MySQL configurada no painel NR (× 3 ambientes)
- [ ] (Opcional) Alert policies configuradas
