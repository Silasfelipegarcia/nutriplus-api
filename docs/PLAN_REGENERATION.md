# Regeneração controlada de plano alimentar

Este documento descreve as regras de produto e o contrato da API para gerar um novo plano alimentar.

## Visão geral

A geração de plano passa por duas camadas:

1. **Política de regeração** (`PlanRegenerationPolicyService`) — define *quando* o usuário pode regerar e por qual motivo.
2. **Cota de assinatura** (`MealPlanGenerationQuotaService`) — teto extra (Free: 1/mês; Atleta: 3/dia).

Ordem em `MealPlanService.enqueueGeneration()`:

```
PlanRegenerationPolicyService.assertAllowed(user, reason)
→ MealPlanGenerationQuotaService.assertCanGenerate(user)
→ enfileirar job + consumir flags do motivo
```

Após geração **COMPLETED**, `plan_regen_locked_until = hoje + progressReviewIntervalDays` (15 dias).

## Motivos (`PlanRegenerationReason`)

| Motivo | Quando é permitido | Consumo |
|--------|-------------------|---------|
| `FIRST_PLAN` | Usuário ainda não tem plano | Nenhum lock extra |
| `GENERATION_RETRY` | Job anterior falhou | Não consome correção/ciclo |
| `ATHLETE_SWITCH` | `athlete_regen_eligible == true` (setado ao ativar modo atleta) | Consome flag de atleta |
| `ONE_TIME_CORRECTION` | `one_time_correction_used_at == null` | Marca correção única usada |
| `CYCLE_REVIEW` | Reavaliação due + review COMPLETED recente + `planChangeSuggested == true` + `reviewId` válido | Consome token do review |
| `NUTRITIONIST_BYPASS` | Fluxo interno PRO/nutricionista | Ignora locks de produto |
| `PLAN_RESET` | Usuário confirma zerar plano atual e gerar outro | Apaga tracking da era atual; reinicia lock de 15 dias; **não** consome correção única |

## Zerar plano (`PLAN_RESET`)

Fluxo de produto para voltar do zero sem esperar 15 dias ou consumir a correção única.

### O que acontece

1. Cliente exibe confirmação destrutiva (checkbox + digitar `ZERAR PLANO`).
2. `POST /meal-plans/generate` com `{ "reason": "PLAN_RESET" }`.
3. API apaga **tracking da era do plano atual** (latest):
   - check-ins das refeições desse plano
   - extras alimentares desde `plan_date`
   - reavaliações e medidas corporais desde o início desse plano
4. Enfileira geração de um **novo** plano (plano anterior permanece no histórico).
5. Ao concluir, `plan_regen_locked_until = hoje + 15` (ciclo reinicia).

### Campos extras em `GET /meal-plans/regeneration-eligibility`

```json
{
  "planResetAvailable": true,
  "currentPlanStarted": false,
  "currentPlanCheckinCount": 0,
  "currentPlanDaysActive": 3
}
```

Usados pelo app para montar o texto de confirmação.

## Endpoints

### `GET /meal-plans/regeneration-eligibility`

Retorna:

```json
{
  "allowedReasons": ["FIRST_PLAN"],
  "lockedUntil": "2026-07-13",
  "daysUntilUnlock": 5,
  "oneTimeCorrectionAvailable": false,
  "athleteRegenAvailable": false,
  "reviewDue": false,
  "daysUntilReview": 5,
  "nextReviewDue": "2026-07-13",
  "hasMealPlan": true,
  "pendingCycleReviewId": null
}
```

### `POST /meal-plans/generate`

Body obrigatório:

```json
{
  "reason": "CYCLE_REVIEW",
  "reviewId": 42
}
```

`reviewId` é obrigatório apenas para `CYCLE_REVIEW`.

## Reavaliação de 15 dias

### `POST /progress/reviews`

Body opcional:

```json
{
  "physicalDiscomforts": "Fome excessiva; Cansaço",
  "positiveChanges": "Mais energia; Roupa folgando",
  "generalNotes": "Texto livre"
}
```

Resposta inclui:

- `planChangeSuggested` — IA sugere mudar o plano?
- `planChangeRationale` — por quê
- `keepPlanMessage` — reforço quando manter
- `confidence` — confiança da análise

O app mobile só permite `CYCLE_REVIEW` se o usuário confirmar após `planChangeSuggested == true`.

## Persistência (migration V48)

Campos em `nutrition_profiles`:

- `one_time_correction_used_at`
- `last_athlete_regen_at`
- `athlete_regen_eligible`
- `plan_regen_locked_until`

Campos em `progress_reviews`:

- `physical_discomforts`, `positive_changes`, `general_notes`
- `plan_change_suggested`, `plan_change_rationale`, `keep_plan_message`, `confidence`
- `plan_regen_consumed`

Campos em `meal_plan_generation_jobs`:

- `regeneration_reason`, `progress_review_id`

## Fluxos no app mobile

1. **Primeiro plano** — `FIRST_PLAN` implícito quando não há plano.
2. **Virou atleta** — dialog após salvar treinos; `ATHLETE_SWITCH` se elegível.
3. **Correção única** — hub nutricional → `PlanCorrectionFlowScreen` → `ONE_TIME_CORRECTION`.
4. **Ciclo 15 dias** — `ProgressReviewScreen` (medidas → sensações → IA → manter/gerar).

Fora desses caminhos, CTAs de regeração exibem mensagem de bloqueio e apontam para Evolução.
