# Elegibilidade de saúde para plano IA

## Objetivo

Bloquear geração/regeneração automática de plano alimentar por IA para perfis de alto risco (gravidez, amamentação, transtorno alimentar em tratamento, restrição renal grave), mantendo o app utilizável (check-ins, evolução, marketplace de nutricionistas).

## Campos (`nutrition_profiles`)

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `pregnancy_status` | `NONE`, `PREGNANT`, `BREASTFEEDING` | Status gestacional |
| `eating_disorder_risk` | boolean | TCA diagnosticado ou em tratamento |
| `severe_renal_restriction` | boolean | Diálise / renal avançada com restrição prescrita |
| `ai_plan_eligible` | boolean | Calculado pelo `HealthEligibilityService` |
| `ai_plan_ineligible_reason` | código | `PREGNANCY`, `BREASTFEEDING`, etc. |

## API

- `HealthEligibilityService.evaluateAndApply()` — ao salvar perfil
- `HealthEligibilityService.assertAiPlanAllowed()` — antes de `POST /meal-plans/generate`
- `GET /meal-plans/regeneration-eligibility` — inclui `aiPlanEligible`, `aiPlanIneligibleReason`, `aiPlanIneligibleMessagePt`
- `GET /legal/health-eligibility` — declaração legal v2026-06-2
- `POST /users/me/accept-terms` — exige `healthEligibilityAccepted` + versão; grava `user_legal_acceptances`

## Mobile

- `HealthEligibilityScreen` — passo 11 do onboarding (antes de condições de saúde)
- `TermsAcceptanceScreen` — terceiro checkbox de elegibilidade
- `ai_plan_eligibility_gate.dart` — bloqueia CTAs de geração + CTA nutricionista

## Agentes

- `POST /api/v1/meal-plan/generate` retorna **422** se `aiPlanEligible=false`

## Usuários existentes

Migration V49 define `ai_plan_eligible=true` retroativamente. Re-aceite legal na próxima versão (`2026-06-2`).

## Fora de escopo (v1)

- Menores de 18 com aceite dos responsáveis
- Bloqueio por diabetes/hipertensão controlada (continua revisão Evandro)
