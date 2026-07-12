---
name: nutriplus-plan-regeneration
description: >-
  Regras de regeração e reset de plano alimentar Nutri+ (RULE-PLAN-001 a 009).
  Use ao implementar POST /meal-plans/generate, elegibilidade, PLAN_RESET,
  correção única, ciclo de 15 dias, modo atleta, cotas Free/Atleta ou UI de
  gerar/zerar plano em API, Flutter ou Web.
---

# Nutri+ — Regeneração de Plano

## Missão

Implementar mudanças em geração/regeneração de plano **sem quebrar política, cota ou UX** nos três clientes.

Fonte da verdade: `nutriplus-api/docs/PLAN_REGENERATION.md` · `RULES_MAP.md` (seção Plano)

## Ordem obrigatória na API

```
PlanRegenerationPolicyService.assertAllowed(user, reason)
→ MealPlanGenerationQuotaService.assertCanGenerate(user)
→ enfileirar job + consumir flags do motivo
```

Código: `MealPlanService.enqueueGeneration()`

## Motivos (`PlanRegenerationReason`)

| Motivo | Quando | Consumo |
|--------|--------|---------|
| `FIRST_PLAN` | Sem plano ainda | Nenhum lock extra |
| `GENERATION_RETRY` | Job anterior falhou | Não consome correção/ciclo |
| `ATHLETE_SWITCH` | `athlete_regen_eligible == true` | Consome flag atleta |
| `ONE_TIME_CORRECTION` | `one_time_correction_used_at == null` | Marca correção única |
| `CYCLE_REVIEW` | Review due + `planChangeSuggested` + `reviewId` | Consome token review |
| `NUTRITIONIST_BYPASS` | Fluxo Pro/nutricionista | Ignora locks produto |
| `PLAN_RESET` | Usuário confirma zerar | Apaga tracking era atual; **não** consome correção única |
| `UNLOCKED_REGEN` | Após desbloqueio elegível | — |

Após **COMPLETED**: `plan_regen_locked_until = hoje + 15 dias`.

## PLAN_RESET — checklist

**API** (`PlanResetService`):
- Apaga check-ins, extras, reviews e medidas da **era do plano atual**
- Plano anterior permanece no histórico
- Enfileira novo plano; ao concluir, lock 15 dias reinicia

**UI (Flutter + Web)** — `RULE-PLAN-006`:
- Confirmação destrutiva: checkbox + digitar `ZERAR PLANO`
- Entry: `PlanResetFlowScreen`, hub tile, portal `plan-reset-entry`

**Nunca:** corpo branco na aba Plano após reset — ver `nutriplus-client-ux`.

## Cotas (além da política)

| Plano | Cota |
|-------|------|
| Free | 1 geração/mês |
| Atleta | 3 geração/dia |

## Elegibilidade IA

`ai_plan_eligible=false` bloqueia geração (`RULE-HEALTH-001`). Ver `HEALTH_ELIGIBILITY.md`.

`GET /meal-plans/regeneration-eligibility` — usar nos clientes antes de CTAs.

## Flag dev

`UNLIMITED_PLAN_REGEN` bypassa locks (`RULE-PLAN-008`) — só dev/homolog.

## Checklist multi-repo

Ao mudar regra ou motivo:

- [ ] `PlanRegenerationPolicyService` / enums API
- [ ] `GET /meal-plans/regeneration-eligibility` (campos novos?)
- [ ] Flutter: `plan_regeneration_gate.dart`, flows de correção/reset
- [ ] Web: paridade portal
- [ ] Testes: `MealPlanFlowIntegrationTest` ou equivalente
- [ ] Atualizar `RULES_MAP.md` se nova regra

## Referência

Detalhes e JSON de elegibilidade: [reference/plan-regeneration-detail.md](reference/plan-regeneration-detail.md)
