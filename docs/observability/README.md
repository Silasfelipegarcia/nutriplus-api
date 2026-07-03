# Observabilidade Nutri+ — dashboards e alertas

Complementa [`OBSERVABILITY.md`](../OBSERVABILITY.md).

## New Relic

Setup completo (APM, logs, MySQL): [`NEW_RELIC.md`](./NEW_RELIC.md).

Queries NRQL para investigar erros: [`NRQL_COOKBOOK.md`](./NRQL_COOKBOOK.md).

Prometheus/Grafana **permanecem ativos** — NR é camada adicional.

## Prometheus — métricas novas

| Métrica | Tags | Uso |
|---------|------|-----|
| `nutriplus.http.server.duration` | `flow`, `sync`, `status` | SLO 300ms em endpoints sync |
| `nutriplus.ai.agent.duration` | `path`, `status` | Latência API → agente |
| `nutriplus_agent_llm_duration_seconds` | `provider`, `agent` | Latência LLM no agente |

Config SLO em `application.properties`:

- `nutriplus.observability.sync-slow-request-ms=300`
- `management.metrics.distribution.slo.nutriplus.http.server.duration=300ms`

## Alertas

Importar [`prometheus-alerts.yml`](./prometheus-alerts.yml) no Prometheus/Alertmanager:

- **NutriplusSyncHttpP95Slow** — p95 sync > 300ms por `flow`
- **NutriplusSyncHttpErrorRate** — 5xx sync > 5%
- **NutriplusAiAgentP95Slow** — IA > 30s
- **NutriplusAgentLlmP95Slow** — LLM agente > 30s

## Dashboards Grafana

| Dashboard | Arquivo | Fonte |
|-----------|---------|-------|
| Operação | [`grafana-dashboard-operations.json`](./grafana-dashboard-operations.json) | Prometheus |
| Negócio / funil | [`grafana-dashboard-business.json`](./grafana-dashboard-business.json) | PostgreSQL (`product_events`, `audit_log`) |

### Importação

1. Grafana → Dashboards → Import
2. Upload do JSON
3. Ajustar datasource (`Prometheus`, `PostgreSQL`)

## Queries úteis — funil onboarding

```sql
-- Drop-off por step (views vs completed)
SELECT v.step,
       COUNT(DISTINCT v.session_id) AS viewed,
       COUNT(DISTINCT c.session_id) AS completed
FROM product_events v
LEFT JOIN product_events c
  ON c.session_id = v.session_id
 AND c.event_name = 'onboarding_step_completed'
 AND c.step = v.step
WHERE v.event_name = 'onboarding_step_viewed'
  AND v.created_at >= NOW() - INTERVAL '7 days'
GROUP BY v.step
ORDER BY MIN(v.created_at);
```

## Melhoria contínua (semanal)

1. Top 5 `flow` com WARN `sync-slow` nos logs API
2. Maior drop-off no funil onboarding (`product_events`)
3. p95 `nutriplus.http.server.duration{sync="true"}` vs SLO 300ms
4. Taxa `meal_plan_generation_failed` / `meal_plan_generation_started`
