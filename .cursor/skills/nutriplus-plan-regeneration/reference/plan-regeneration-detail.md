# PLAN_REGENERATION — Detalhe

## Campos extras em `GET /meal-plans/regeneration-eligibility`

```json
{
  "planResetAvailable": true,
  "currentPlanStarted": false,
  "currentPlanCheckinCount": 0,
  "currentPlanDaysActive": 3,
  "aiPlanEligible": true,
  "aiPlanIneligibleReason": null,
  "oneTimeCorrectionAvailable": true,
  "regenLockedUntil": "2026-07-27"
}
```

## Edição de perfil com lock

`RULE-HEALTH-002`: edição que afeta plano respeita lock de 15 dias.

Flutter/Web: `ProfileEditEligibility` dialog antes de salvar.

## Pós-save perfil

Se mudança dispara nova geração: `NutriBusyButton` + fluxo Tier C.

## Código principal

| Peça | Caminho |
|------|---------|
| Política | `PlanRegenerationPolicyService` |
| Cota | `MealPlanGenerationQuotaService` |
| Reset | `PlanResetService` |
| Enqueue | `MealPlanService.enqueueGeneration` |
| Flutter gate | `plan_regeneration_gate.dart` |
| Flutter launcher | `meal_plan_generation_launcher.dart` |

## IDs de regra (PRs/postmortems)

`RULE-PLAN-001` … `RULE-PLAN-009` — ver `docs/RULES_MAP.md`
