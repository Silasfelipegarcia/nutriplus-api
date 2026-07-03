# Nutri+ — Observabilidade e trace distribuído

Guia técnico de logs, métricas, trace e auditoria. Complementa o modelo C4 em [`C4.md`](./C4.md).

> **Manutenção:** qualquer mudança em filters, headers, MDC, `TraceContext`, `AiAgentClient`, logback ou contrato com o agente/frontend **deve** atualizar este arquivo e a seção de maturidade em `C4.md`.

---

## Visão de produto

O usuário final vê, em caso de erro, uma mensagem amigável e opcionalmente um **ID de correlação** (`correlationId`) para informar ao suporte.

A equipe interna consegue:

- Seguir **um request** da API até o agente usando `X-Correlation-Id` / `X-Trace-Id`.
- Entender **qual ação** falhou via `X-Flow-Id` (ex.: `generate-meal-plan`).
- Agrupar comportamento do **mesmo dispositivo/sessão** via `X-Session-Id`.
- Auditar ações sensíveis em `audit_log` e chamadas IA em `ai_requests_log`.

---

## Contrato de headers HTTP

| Header | Escopo | Gerado por | MDC (API) | Persistido no DB |
|--------|--------|------------|-----------|------------------|
| `X-Correlation-Id` | 1 request HTTP | Cliente (novo por call) | `correlationId` | `ai_requests_log`, `audit_log` |
| `X-Trace-Id` | 1 request HTTP | Cliente (novo por call) | `traceId` | Não (apenas logs) |
| `X-Flow-Id` | Ação de negócio | Cliente | `flowId` | Não |
| `X-Session-Id` | Sessão do app | Cliente (persistido) | `sessionId` | Não |
| `Idempotency-Key` | 1 gesto / retry | Cliente (UUID por ação) | `idempotencyKey` | `idempotency_keys` (API) |

### Idempotência (`Idempotency-Key`)

| Ambiente | Comportamento |
|----------|---------------|
| `local`, `dev`, `test` | `idempotency.enabled=false` — pass-through, **localhost não trava** |
| `homolog`, `prod` | Obrigatória em mutações (`POST`/`PUT`/`PATCH`/`DELETE`), exceto login/refresh/webhooks |

| Código HTTP | Significado |
|-------------|-------------|
| `400` + `IDEMPOTENCY_KEY_REQUIRED` | Header ausente em mutação |
| `409` + `IDEMPOTENCY_IN_PROGRESS` | Mesma key ainda processando |
| `422` + `IDEMPOTENCY_KEY_BODY_MISMATCH` | Mesma key, body diferente |
| `2xx/4xx` replay | Header `Idempotency-Replayed: true` + body cacheado |

Excluídos: `GET`, `HEAD`, `OPTIONS`, `/health`, `/actuator/**`, `/webhooks/**`, `/auth/login`, `/auth/refresh`.

Config (12-factor): `IDEMPOTENCY_ENABLED`, `IDEMPOTENCY_REQUIRE_KEY`, `IDEMPOTENCY_TTL_HOURS`, `IDEMPOTENCY_IN_PROGRESS_TIMEOUT`.


| Situação | O que volta |
|----------|-------------|
| Qualquer resposta da API | Headers `X-Correlation-Id`, `X-Trace-Id` |
| Replay idempotente | Header `Idempotency-Replayed: true` |
| Erro JSON (`4xx`/`5xx`) | Body com `correlationId`, `traceId`, `message`, `code` |
| Resposta do agente | Headers `X-Correlation-Id`, `X-Trace-Id` |

---

## Visão técnica — API (`nutriplus-api`)

### Pipeline de filters (ordem)

**Rotas públicas** (`/auth/**`, `/health`, …):

1. `CorrelationIdFilter` — MDC trace
2. `RequestPerformanceFilter` — duração
3. `IdempotencyFilter` — dedup mutações (desligado em `dev`)
4. `RateLimitFilter`

**Rotas autenticadas**:

1. `CorrelationIdFilter`
2. `RequestPerformanceFilter`
3. `RateLimitFilter`
4. OAuth2 Bearer JWT
5. `MdcUserFilter` — `userId` no MDC
6. `PasswordMustChangeFilter`
7. `IdempotencyFilter` — após auth (escopo por `userId`)

### Formato de log

**Local / dev** (`application.properties`):

```
%d [%thread] %-5level %logger [cid=%X{correlationId} trace=%X{traceId} flow=%X{flowId} session=%X{sessionId} user=%X{userId}] - %msg
```

**Produção** (`logback-spring.xml`, profiles `prod` e `homolog`):

- `LogstashEncoder` — JSON estruturado com campos MDC.

### Chamadas ao agente

`AiAgentClient` adiciona `TraceContext.currentHeaders()` em todo `POST`:

```java
TraceContext.currentHeaders().forEach(builder::header);
```

Métricas Micrometer:

- `nutriplus.ai.agent.duration` (timer, tags `path`, `status`)
- `nutriplus.ai.agent.calls` (counter)
- `nutriplus.meal_plan.generation.duration` (timer)

### Actuator

| Endpoint | Uso |
|----------|-----|
| `/actuator/health/liveness` | Liveness (Railway) |
| `/actuator/health/readiness` | Readiness (db + aiAgent) |
| `/actuator/prometheus` | Métricas (autenticado) |

### Persistência de auditoria

| Tabela | Campos de trace | Eventos |
|--------|-----------------|---------|
| `audit_log` | `correlation_id` | LOGIN, REGISTER, TOKEN_REFRESH, MEAL_PLAN_GENERATED, … |
| `ai_requests_log` | `correlation_id` | CALCULATE_MACROS, GENERATE_MEAL_PLAN |
| `product_events` | `correlation_id`, `session_id` | onboarding_step_*, meal_plan_* (funil) |

---

## Visão técnica — Agente (`nutriplus-agentes`)

### `TraceMiddleware`

- Lê os 4 headers (gera cid/trace se ausentes).
- Log INFO por request: método, path, status, duração, cid, trace, flow, session.
- Incrementa `REQUEST_COUNT` e `REQUEST_LATENCY` (Prometheus).
- Ecoa `X-Correlation-Id` e `X-Trace-Id` na resposta.

### Personas e LLM

- Registry: `config/agents.yaml` — **Luna** e **Bruno**
- Provider padrão: **Groq** (`LLM_PROVIDER=groq`, `GROQ_API_KEY`)
- Métrica `LLM_CALLS` — labels: `groq`, `openai`, `mock`, `guardrail_fallback`

### Limitações atuais

1. ~~**Logs do `MealPlanGenerator`** não incluem cid/trace~~ — corrigido via `contextvars` + `asyncio.copy_context()` em `to_thread`.
2. **Formato texto plano** — não há JSON estruturado como na API em prod.
3. **Chamadas Groq/OpenAI** não propagam trace externo (sem OTel).

Métricas LLM: `nutriplus_agent_llm_duration_seconds` (histogram por `provider`, `agent`).

---

## Visão técnica — Frontend (`nutriplus-frontend`)

### `TraceService`

- `sessionId`: UUID v4 persistido em `SharedPreferences`.
- `newCorrelationId()` / `newTraceId()`: novo UUID **a cada** chamada HTTP.
- `headers(flowId:)`: monta os 4 headers.

### `ApiService`

Cada método define `flowId` explícito (ex.: `generate-meal-plan`).

### Tratamento de erro

`ApiException` expõe `correlationId` lido do body JSON; `traceId` ainda não é exposto ao UI.

### Gap conhecido: retry 401

~~Em `_authorized`, após `refreshToken()`, a segunda tentativa chama `_headers()` de novo e **gera novos** cid/trace~~ — corrigido: headers são reutilizados no retry.

### Product analytics

- `POST /analytics/events` — batch de eventos (`sessionId`, `events[]`)
- App Flutter: `ProductAnalyticsService` + mixin `OnboardingStepAnalytics`
- Dashboards: [`docs/observability/README.md`](./observability/README.md)

---

## Como investigar um incidente (runbook)

### 0. New Relic (prod/homolog com agent ativo)

Receitas NRQL em [`observability/NRQL_COOKBOOK.md`](./observability/NRQL_COOKBOOK.md).

Com `correlationId` do usuário:

```sql
SELECT timestamp, message, correlationId, traceId, flowId, userId, httpPath, httpStatus
FROM Log
WHERE entity.name = 'nutriplus-api-prod'
  AND correlationId = '<cid>'
SINCE 24 hours ago
LIMIT 100
```

```sql
SELECT name, duration, error, correlationId, flowId, userId
FROM Transaction
WHERE appName = 'nutriplus-api-prod'
  AND correlationId = '<cid>'
SINCE 24 hours ago
```

Distributed tracing: APM → transaction → spans (API → agente → JDBC).

### 1. Usuário reporta erro com `correlationId`

```text
grep "<correlationId>" api-logs
grep "<correlationId>" agent-logs
```

### 2. Busca por jornada de produto

```text
grep "flowId=generate-meal-plan" api-logs
grep "flow=generate-meal-plan" agent-logs
```

### 3. Sessão do app

```text
grep "sessionId=<uuid>" api-logs
```

### 4. Banco de dados

```sql
SELECT * FROM ai_requests_log WHERE correlation_id = '<cid>' ORDER BY created_at DESC;
SELECT * FROM audit_log WHERE correlation_id = '<cid>' ORDER BY created_at DESC;
```

### 5. Métricas

- Grafana / Prometheus: `nutriplus_ai_agent_duration_seconds`, `nutriplus_meal_plan_generation_duration_seconds`.
- New Relic APM (quando `NEW_RELIC_LICENSE_KEY` configurada): transactions, distributed tracing, JDBC spans.

---

## New Relic (APM + logs + MySQL)

### NewRelicTraceBridge (modo defensivo)

`NewRelicTraceBridge` (`infrastructure/observability/NewRelicTraceBridge.java`) adiciona custom attributes ao MDC/NR sem derrubar requests se o agent Java não estiver no classpath:

- Tenta carregar `com.newrelic.api.agent.NewRelic` uma vez no boot.
- Em falha (`ClassNotFoundException`), seta flag `disabled` e ignora chamadas subsequentes.
- `newrelic-api` deve estar no **fat jar** (não `provided` scope) — ver [RELEASE_NOTES_2026-07.md](./RELEASE_NOTES_2026-07.md).

Chamado por `CorrelationIdFilter` / `MdcUserFilter` / `RequestPerformanceFilter`.

Receitas NRQL: [`observability/NRQL_COOKBOOK.md`](./observability/NRQL_COOKBOOK.md). Setup: [`observability/NEW_RELIC.md`](./observability/NEW_RELIC.md).

Camada adicional ao Prometheus/Grafana. **Mesma conta/license key do `lupa-cnpj-api`** — copie `NEW_RELIC_LICENSE_KEY` do Railway do Lupa para os services Nutri+.

Detalhes de setup em [`observability/NEW_RELIC.md`](./observability/NEW_RELIC.md).

### Serviços instrumentados

| Serviço | Mecanismo | Ativa quando |
|---------|-----------|--------------|
| `nutriplus-api` | Java agent v9.3.0 via `docker-entrypoint.sh` (padrão Lupa) | `NEW_RELIC_LICENSE_KEY` definida |
| `nutriplus-agentes` | Python agent (`newrelic-admin run-program`) | idem |
| MySQL (Railway) | JDBC spans (automático) + DB Monitoring (painel NR) | agent na API + usuário `newrelic` no MySQL |

### Nomes de app (Railway)

| Ambiente | API | Agente |
|----------|-----|--------|
| develop | `nutriplus-api-dev` | `nutriplus-agentes-dev` |
| homolog | `nutriplus-api-homolog` | `nutriplus-agentes-homolog` |
| prod | `nutriplus-api-prod` | `nutriplus-agentes-prod` |

### Correlação cross-service

Os mesmos IDs do contrato HTTP aparecem como atributos pesquisáveis:

| ID | API (MDC / logs JSON) | Agente (logs + NR custom attrs) | Mobile |
|----|----------------------|--------------------------------|--------|
| `correlationId` | Sim (MDC + NR custom attrs) | Sim | Header `X-Correlation-Id` |
| `traceId` | Sim | Sim | Header `X-Trace-Id` |
| `flowId` | Sim | Sim | Header `X-Flow-Id` |
| `sessionId` | Sim | Sim | Header + Crashlytics `session_id` |
| `app_env` | — | — | Crashlytics custom key |

**Fluxo de investigação com NR:**

1. Usuário reporta crash → Firebase Crashlytics com `session_id` e `app_env`.
2. NR Logs: buscar `sessionId:<uuid>` na API ou `correlationId:<cid>` se disponível.
3. Distributed tracing: seguir span API → agente → JDBC na mesma transação.

### Mobile — Firebase Crashlytics

Crashes do app Flutter vão para Firebase (não New Relic). Setup em `nutriplus-frontend/docs/DEPLOYMENT.md#firebase-crashlytics`.

---

## Matriz de garantias E2E

| Cenário | Garantido? |
|---------|------------|
| FE → API, mesmo request | Sim (cid + trace + flow + session no MDC) |
| API → Agente, mesmo request | Sim (TraceContext) |
| Log HTTP do agente com ids | Sim (TraceMiddleware) |
| Log LLM dentro do agente | Sim (contextvars + logging filter) |
| Jornada multi-tela (vários requests) | Parcial (`sessionId` + `flowId` + `product_events`) |
| Retry 401 no app | Sim (headers reutilizados) |
| Erro no app com cid | Sim |
| Erro no app com traceId | Sim (`ApiException.traceId`) |
| Auditoria DB com trace completo | Parcial (correlationId; product_events tem session) |
| SLO 300ms endpoints sync | Sim (WARN + Prometheus histogram) |
| Idempotency-Key em mutações (homolog/prod) | Sim |
| Localhost sem bloqueio de idempotência | Sim (`dev`: `idempotency.enabled=false`) |
| Replay seguro (retry / duplo clique) | Sim (`idempotency_keys` + header replay) |
| APM New Relic (API + agente) | Sim (quando license key no Railway) |
| Crash reporting mobile | Sim (Firebase Crashlytics em release) |

---

## Runbook — incidente e ataque

### Usuário reporta erro

1. Obter `correlationId` do app (rodapé do erro: `Suporte: #...`) ou JSON da API.
2. Logs API: `grep "<correlationId>"` → path, duração, `userId`.
3. Logs agente: mesmo `X-Correlation-Id`.
4. SQL:
   ```sql
   SELECT * FROM ai_requests_log WHERE correlation_id = '...';
   SELECT * FROM security_events ORDER BY created_at DESC LIMIT 20;
   ```

### Sinais de ataque (manual / alertas)

| Sinal | Onde | Ação |
|-------|------|------|
| Spike 429 | Logs `RATE_LIMIT_EXCEEDED` | Ver IP em `security_events`; ajustar WAF |
| Spike 401 `/auth/login` | Logs + `security_events` | Possível credential stuffing |
| Alert `NutriplusSecurityRiskSpike` | Prometheus | Revisar `security_events` blocked=true |
| Jobs FAILED `AI_AGENT_ERROR` | `meal_plan_generation_jobs` | Agente down ou circuit breaker aberto |
| CPU/RAM Railway | Dashboard | Escalar ou limitar Tier C |

### Health pós-deploy

```bash
curl -s http://localhost:8080/actuator/health/readiness | jq .
curl -s http://localhost:8000/health | jq .
```

Prometheus (com token):

```bash
curl -s -H "X-Metrics-Token: $METRICS_SCRAPE_TOKEN" http://localhost:8080/actuator/prometheus | head
```

---

## Roadmap

| Prioridade | Item | Repositório | Status |
|------------|------|-------------|--------|
| P1 | Reutilizar cid/trace no retry `_authorized` | frontend | Feito |
| P1 | `contextvars` + logging filter no agente (incl. `to_thread`) | agentes | Feito |
| P1 | SLO sync 300ms (WARN + histogram) | api | Feito |
| P1 | `product_events` + `/analytics/events` | api + frontend | Feito |
| P1 | `Idempotency-Key` API + Flutter + agente | todos | Feito |
| P1 | New Relic APM (API Java + agente Python) | api + agentes | Feito |
| P1 | Firebase Crashlytics (mobile) | frontend | Feito |
| P2 | MySQL Database Monitoring no painel NR | infra | Pendente (manual) |
| P2 | Colunas `trace_id`, `flow_id`, `session_id` em `ai_requests_log` | api | Pendente |
| P2 | Log INFO estruturado no agente (JSON em prod) | agentes |
| P2 | Expor `traceId` no `ApiException` | frontend |
| P3 | `traceId` estável por sessão de app (opcional) | frontend |
| P3 | OpenTelemetry + `traceparent` | todos |

---

## Checklist de PR (observabilidade)

Ao abrir PR que toca trace ou logs:

- [ ] Headers de entrada/saída documentados aqui se mudaram
- [ ] `C4.md` atualizado se mudou container/componente
- [ ] Teste manual: um request com header fixo chega ao agente com o mesmo cid
- [ ] Erro JSON ainda inclui `correlationId` / `traceId`
- [ ] Profile `prod` continua emitindo JSON (API)
