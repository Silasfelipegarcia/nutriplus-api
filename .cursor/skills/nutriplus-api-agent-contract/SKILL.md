---
name: nutriplus-api-agent-contract
description: >-
  Contrato JSON entre nutriplus-api e nutriplus-agentes (RULE-AG-003). Use ao
  adicionar ou alterar campos em schemas, AiAgentClient, meal-plan generate,
  progress analyze ou qualquer endpoint que cruza API e agentes.
---

# Nutri+ — Contrato API ↔ Agentes

## Regra de ouro

**Mudou schema → API e agentes sobem juntos.** Campo novo não pode quebrar deserialize.

`RULE-AG-003` · `nutriplus-agentes/app/schemas.py` · `AiAgentClient.java`

## Arquitetura

- Clientes **nunca** chamam agentes diretamente.
- API orquestra via `AiAgentClient` com circuit breaker e headers de trace.

## Naming JSON

Agentes: `CamelModel` com `alias_generator=to_camel` em `schemas.py`.

API: `profileToMap()` e DTOs em **camelCase** — chaves devem bater.

## Headers obrigatórios

`X-Correlation-Id`, `X-Trace-Id`, `X-Flow-Id`, `X-Session-Id`, `Idempotency-Key` (mutations)

## Endpoints principais

| Endpoint agente | Chamador API |
|-----------------|--------------|
| `POST /api/v1/nutrition/calculate` | Determinístico macros |
| `POST /api/v1/meal-plan/generate` | Worker geração plano |
| `POST /api/v1/progress/analyze` | Evolução |
| `POST /api/v1/food-extra/estimate` | Extras fora do plano |
| `POST /api/v1/training/consult` | Garcia (interno) |
| `POST /api/v1/substitutions/generate` | Substituições |

## MealPlanGenerateRequest — campos sensíveis

Opcionais no prompt **só quando presentes** (`RULE-AG-001`):

`nutritionistNotes`, `foodDislikes`, `healthConditions`, `allergies`, `mealNotes`, `trainingActivities`, `sharedFromPlan`, `chewingDifficulty`, `aiPlanEligible`

Não duplicar perfil inteiro se API já envia o necessário.

## MealPlanGenerateResponse

`meals[]`, `shoppingList[]` (com `swapOptions`), `shoppingGuidance`, reviews (`medicalReview*`, `dietReview*`, `seniorReview*`).

## Checklist de PR

- [ ] `schemas.py` — Pydantic models + aliases
- [ ] `AiAgentClient` — mapping request/response
- [ ] DTOs Java se expostos na API
- [ ] `pytest` agentes com mock LLM (`USE_MOCK_LLM=true`)
- [ ] `MealPlanFlowIntegrationTest` ou teste de contrato API
- [ ] Flutter/Web só se campo chega ao cliente

## Latência

Tier C ~30s. Cada +1s no agente = +1s percebido (`RULE-AG-001`, `RULE-AG-002`).

Payload enxuto; pós-processamento O(n) sobre refeições.

## Docs

`nutriplus-api/docs/INTEGRATIONS.md` · `nutriplus-agentes/docs/architecture.md`
