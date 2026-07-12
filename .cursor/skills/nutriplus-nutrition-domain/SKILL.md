---
name: nutriplus-nutrition-domain
description: >-
  Domínio nutricional Nutri+: BMR/TDEE, calculationMethod, macros, modo atleta,
  low carb, idoso, elegibilidade IA (ai_plan_eligible), personas Luna/Bruno.
  Use ao alterar nutrition-profile, nutrition/calculate, metas calóricas, perfil
  de saúde, onboarding medidas ou lógica clínica em API, agentes ou clientes.
---

# Nutri+ — Domínio Nutricional

## Missão

Implementar mudanças em metabolismo, perfil e plano respeitando **cálculo determinístico + revisão clínica IA**, sem confundir meta alimentar com evolução corporal.

Docs: `METABOLISM_AND_BODY_COMPOSITION.md` · `HEALTH_ELIGIBILITY.md` · `TRAINING_MODE.md`

## Personas e assistentes

| Persona | Assistente | Notas |
|---------|------------|-------|
| Usuário comum | Luna ou Bruno | Plano IA padrão |
| Atleta | Luna/Bruno + Garcia | MET, fome por refeição, `ATHLETE_SWITCH` |
| Idoso 60+ | + Helena | Textura, proteína, hidratação |
| Inelegível IA | Sem plano auto | CTA nutricionista; app continua utilizável |

## Modos de cálculo (`calculationMethod`)

| Modo | BMR |
|------|-----|
| `ESTIMATE` | Mifflin-St Jeor |
| `BIOIMPEDANCE` | Katch-McArdle (% gordura) |
| `MANUAL_BMR` | Valor informado pelo usuário |

Pipeline: `POST /api/v1/nutrition/calculate` → salva em `nutrition_profiles` → `POST /meal-plans/generate` com `refreshMacroTargets`.

**Não confundir:** modo de cálculo = meta do plano; `body_measurement_sessions` = evolução/histórico.

## Elegibilidade IA (`RULE-HEALTH-*`)

`ai_plan_eligible=false` bloqueia:
- `POST /meal-plans/generate` (API + agentes 422)
- CTAs de geração nos clientes (`ai_plan_eligibility_gate.dart`)

Motivos: gravidez, amamentação, TCA, restrição renal grave.

Disclaimer obrigatório — IA não substitui nutricionista (`RULE-HEALTH-003`).

## Modo atleta

- `athleteModeEnabled`, atividades com MET
- Garcia: `POST /api/v1/training/consult` (interno, voz coach)
- `athleteHungerByMeal` no prompt
- Regeneração: `ATHLETE_SWITCH` quando `athlete_regen_eligible`

## Low carb (`nutritionMode`)

Almoço/jantar: proteína + vegetais; carbos moderados ou restritos conforme meta.

Extras fora do plano somam carbs em low carb (`RULE-CHK-002`).

## Agentes e segurança

- Luna/Bruno: **não recalculam** macros — usam `[SISTEMA_MACROS]`
- Evandro: revisão clínica pós-geração
- Regras críticas também em `guardrails.py` — não só no prompt

## Checklist multi-repo

- [ ] `nutrition_engine.py` / `NutritionProfile` API
- [ ] `schemas.py` se novos campos no generate
- [ ] Onboarding / edição perfil (Flutter + Web)
- [ ] `HealthEligibilityService` se afeta elegibilidade
- [ ] Testes: `test_nutrition_engine.py`, integração API

## Referência

Campos de perfil e guardrails de emagrecimento: [reference/nutrition-fields.md](reference/nutrition-fields.md)
