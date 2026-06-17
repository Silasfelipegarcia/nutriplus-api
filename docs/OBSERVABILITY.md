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

### Resposta HTTP

| Situação | O que volta |
|----------|-------------|
| Qualquer resposta da API | Headers `X-Correlation-Id`, `X-Trace-Id` |
| Erro JSON (`4xx`/`5xx`) | Body com `correlationId`, `traceId`, `message`, `code` |
| Resposta do agente | Headers `X-Correlation-Id`, `X-Trace-Id` |

---

## Visão técnica — API (`nutriplus-api`)

### Pipeline de filters (ordem)

**Rotas públicas** (`/auth/**`, `/health`, …):

1. `CorrelationIdFilter` — MDC trace
2. `RequestPerformanceFilter` — duração
3. `RateLimitFilter`

**Rotas autenticadas**:

1. `CorrelationIdFilter`
2. `RequestPerformanceFilter`
3. `RateLimitFilter`
4. OAuth2 Bearer JWT
5. `MdcUserFilter` — `userId` no MDC
6. `PasswordMustChangeFilter`

### Formato de log

**Local / dev** (`application.properties`):

```
%d [%thread] %-5level %logger [cid=%X{correlationId} trace=%X{traceId} flow=%X{flowId} session=%X{sessionId} user=%X{userId}] - %msg
```

**Produção** (`logback-spring.xml`, profile `prod`):

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

---

## Visão técnica — Agente (`nutriplus-agentes`)

### `TraceMiddleware`

- Lê os 4 headers (gera cid/trace se ausentes).
- Log INFO por request: método, path, status, duração, cid, trace, flow, session.
- Incrementa `REQUEST_COUNT` e `REQUEST_LATENCY` (Prometheus).
- Ecoa `X-Correlation-Id` e `X-Trace-Id` na resposta.

### Limitações atuais

1. **Logs do `MealPlanGenerator`** não incluem cid/trace (thread worker via `asyncio.to_thread`).
2. **Formato texto plano** — não há JSON estruturado como na API em prod.
3. **Chamadas OpenAI** não propagam trace para a OpenAI (sem OTel).

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

Em `_authorized`, após `refreshToken()`, a segunda tentativa chama `_headers()` de novo e **gera novos** cid/trace, quebrando a correlação do request original.

---

## Como investigar um incidente (runbook)

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

---

## Matriz de garantias E2E

| Cenário | Garantido? |
|---------|------------|
| FE → API, mesmo request | Sim (cid + trace + flow + session no MDC) |
| API → Agente, mesmo request | Sim (TraceContext) |
| Log HTTP do agente com ids | Sim (TraceMiddleware) |
| Log LLM dentro do agente | Não |
| Jornada multi-tela (vários requests) | Parcial (`sessionId` + `flowId` diferentes) |
| Retry 401 no app | Não (novos ids) |
| Erro no app com cid | Sim |
| Erro no app com traceId | Não (não parseado no Flutter) |
| Auditoria DB com trace completo | Parcial (só correlationId) |

---

## Roadmap

| Prioridade | Item | Repositório |
|------------|------|-------------|
| P1 | Reutilizar cid/trace no retry `_authorized` | frontend |
| P1 | `contextvars` + logging filter no agente (incl. `to_thread`) | agentes |
| P2 | Colunas `trace_id`, `flow_id`, `session_id` em `ai_requests_log` | api |
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
