---
name: nutriplus-performance-gate
description: >-
  Performance e latência Nutri+ (Tier S/A/B/C, cache, N+1, baseline k6, Flyway
  MySQL). Use ao criar endpoints, migrations, queries, cache, índices ou antes
  de merge em nutriplus-api; validar impacto em dashboard e portal.
---

# Nutri+ — Performance Gate

## SLAs p95

| Tier | Alvo | Exemplos |
|------|------|----------|
| **S** | < 200 ms | bootstrap, checkins, `/users/me`, Pro readonly |
| **A** | < 500 ms | auth, writes leves |
| **B** | < 2 s | macros síncronos, swaps, perfil |
| **C** | < 30 s | geração plano (async) |

Docs: `PERFORMANCE.md` · `LATENCY_GUARDRAILS.md` · `PERFORMANCE_BASELINE.md`

## Antes do merge (API)

```bash
./perf/run-baseline.sh local
```

Regressão máxima: **×1.2** vs baseline warm.

## Regras (`RULE-PERF-*`)

| ID | Regra |
|----|-------|
| RULE-PERF-S1 | Tier S: `@Cacheable` ou justificar `none` |
| RULE-PERF-002 | **Proibido N+1** — `MealLoader` pattern em listas com filhos |
| RULE-PERF-003 | Baseline antes do PR |
| RULE-PERF-004 | Flyway **MySQL**: `BIGINT AUTO_INCREMENT` — sem `SERIAL`/`RETURNING` |

## Cache

- Endpoint Tier S novo → `@Cacheable`, HTTP cache público, ou documentar `none` em `PERFORMANCE.md`
- Write que altera leitura cacheada → `@CacheEvict` no serviço correto
- Não encurtar TTL em prod sem motivo documentado

## Migrations

Query por `user_id` + data → **índice composto** na mesma PR.

## Clientes

- ≤ 3 requests no first paint
- `Promise.all` / `/app/bootstrap` — sem cascata de 5+ GETs
- Polling chat ≥ 8s

## Agentes (Tier C)

Cada +1s no agente = +1s percebido. Payload enxuto; O(n) pós-processamento.

## Checklist PR performance

- [ ] Tier do endpoint identificado e documentado
- [ ] Sem `findAll()` em tabelas que crescem
- [ ] Listas com filhos: batch load
- [ ] Baseline local executado se Tier S afetado
- [ ] Índice se migration adiciona filtro frequente

## Referência

Matriz de endpoints: [reference/endpoint-tiers.md](reference/endpoint-tiers.md)
