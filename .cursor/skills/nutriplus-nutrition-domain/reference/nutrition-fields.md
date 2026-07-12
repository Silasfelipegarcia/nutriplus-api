# Nutrição — Campos e Guardrails

## Campos sensíveis no generate

`healthConditions`, `allergies`, `medications`, `chewingDifficulty`, `lifeStage`, `pregnancy_status`, `eating_disorder_risk`, `severe_renal_restriction`, `seniorWeightLossAck`, `athleteHungerByMeal`, `nutritionMode`, `dietaryPreference`, `restriction`

## Guardrails emagrecimento (agentes)

- Meta não acima do gasto total por piso fixo (1200/1500)
- Déficit mínimo ~250 kcal quando possível
- `align_plan_to_target` após distribuição

## Sincronização meta no plano

API: `refreshMacroTargets` antes do job  
Agentes: `sync_meal_plan_targets`  
Tolerância alinhamento: ±12% após rebalance

## Idoso

`[SISTEMA_IDADE]` no prompt; Helena se ≥65 ou SENIOR; filtro texturas duras se `chewingDifficulty`

## Onboarding saúde

Ordem: elegibilidade (passo 11) → condições → alergias → rotina  
Termos: aceite triplo incluindo `healthEligibilityAccepted`

## Código principal

| Peça | Onde |
|------|------|
| Cálculo macros | `nutrition_engine.py`, `NutritionCalculateService` |
| Elegibilidade | `HealthEligibilityService` |
| Perfil | `nutrition_profiles` table |
| Medidas | `body_measurement_sessions`, `ProgressService` |
| Atleta | `training_consultant.py`, `athlete_meal_timing.py` |
