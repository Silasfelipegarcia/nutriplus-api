# Nutri+ Client — Componentes e Fluxos

## Check-in optimista (Tier A)

`TodayScreen._toggleCheckin`: `_optimisticCheckins` no mesmo frame; reverte + snackbar em erro.

## Prefetch elegibilidade

`prefetchPlanRegenerationEligibility()` em `plan_regeneration_gate.dart`:

- Cache: `AppDataStore.planRegenerationEligibility`
- Dialog leve se fetch > 300ms

Mount: `MealPlanScreen`, `ProfileNutritionHubScreen`.

## Hub perfil — tile loading

`ProfileSettingsTile.loading`: spinner no **trailing do tile tocado**, não spinner global na lista.

## Fix tela branca pós-reset (referência)

Causas encadeadas corrigidas:

1. `_isEmpty=false` + `_plan=null` → `SizedBox.shrink()`
2. `popUntil(isFirst)` destruía flow antes do callback
3. Sync local não limpava `_plan` quando store `null`

Correções: empty state explícito; `_isEmpty = (_plan == null)`; launcher nav+refresh antes de `requestGeneration`.

## Polling

Chat: mínimo **8s**; cancelar no `dispose`.

Geração plano: poll `GET /meal-plans/generation-status` via controller — não agressivo.

## Paridade Flutter ↔ Web

| Fluxo | Flutter | Web |
|-------|---------|-----|
| Zerar plano | `PlanResetFlowScreen` | `plan-reset-entry.component` |
| Geração Tier C | `PlanGeneratingNotice` | `meal_plan_generation.facade.ts` |
| Elegibilidade | `plan_regeneration_gate.dart` | equivalente portal |

## Docs relacionados

- `nutriplus-frontend/docs/ARCHITECTURE.md`
- `nutriplus-web/docs/ARCHITECTURE.md`
- `LATENCY_GUARDRAILS.md` (cliente)
