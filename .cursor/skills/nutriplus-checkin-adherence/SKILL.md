---
name: nutriplus-checkin-adherence
description: >-
  Check-ins, aderência, streak e engajamento Nutri+ (aba Hoje). Use ao implementar
  DONE/SKIPPED, extras fora do plano, stats, mensagens motivacionais, lembretes
  locais ou UI optimista na TodayScreen.
---

# Nutri+ — Check-ins e Aderência

## Regras (`RULE-CHK-*`)

| ID | Regra |
|----|-------|
| RULE-CHK-001 | Check-in por refeição: **DONE** ou **SKIPPED** |
| RULE-CHK-002 | Extras fora do plano somam calorias (e carbs em low carb) |
| RULE-CHK-003 | UI **optimistic** no toggle; reverte + snackbar em erro |
| RULE-CHK-004 | Streak e aderência via `GET /checkins/stats` |

Doc: `ENGAGEMENT.md`

## Aba Hoje — dados

| Elemento | Fonte |
|----------|-------|
| Próxima refeição | `GET /checkins/today` |
| Streak | `checkins/stats` → `currentStreak` |
| Aderência semanal | `weekAdherencePercent` |
| Meta calórica | check-ins + extras |

## UI Tier A (optimistic)

`TodayScreen._toggleCheckin`: `_optimisticCheckins` no **mesmo frame**; reverte em erro API.

Sem spinner para toggle — ver `nutriplus-client-ux`.

## Extras fora do plano

`POST /checkins/extras` — estimativa calorias via agente `food-extra/estimate` (Tier B).

## Mensagens motivacionais

Client-side `MotivationMessages` — **determinísticas**, tom Luna/Bruno:

1. Sem plano → convite gerar
2. Todas marcadas → celebração
3. Refeição atrasada → nudge suave
4. Após 20h sem check-in → streak em risco
5. Sem check-ins → próxima refeição

**Tom:** incentivo, não culpa. Sem pontos/ranking/LLM em tempo real no MVP.

## Lembretes locais (MVP)

- Opt-in explícito; permissão SO ao ativar
- Horários de `meal.scheduled_time`
- Re-sync ao carregar Hoje / plano pronto
- Cancelar ao logout
- Watch/Wear: espelha notificação; ação "OK — feita" → `NotificationCheckinHandler`

## Checklist

- [ ] DONE/SKIPPED persistido corretamente
- [ ] Optimistic UI com rollback
- [ ] Extras atualizam meta calórica do dia
- [ ] Stats coerentes após PLAN_RESET (tracking apagado)

## Evolução

Aderência alimenta aba **Evolução** e ciclo reavaliação ~15 dias — ver `PROGRESS_ANALYSIS.md`, `nutriplus-plan-regeneration` (`CYCLE_REVIEW`).
