# Legal — Versões e Campos

## Versão atual

`nutriplus.legal.version` = `2026-06-2`

## Aceite termos

`POST /users/me/accept-terms` exige:
- termos
- privacidade  
- `healthEligibilityAccepted` + versão elegibilidade

Grava em `user_legal_acceptances`.

## Elegibilidade

`GET /legal/health-eligibility` — declaração v2026-06-2

Campos perfil: `pregnancy_status`, `eating_disorder_risk`, `severe_renal_restriction` → `ai_plan_eligible`

## Telas Flutter

- `HealthEligibilityScreen` — onboarding passo 11
- `TermsAcceptanceScreen` — terceiro checkbox
- `ai_plan_eligibility_gate.dart` — bloqueio CTAs

## Hard delete / freeze

Portal web only — ver `nutriplus-account-lifecycle`.

## Store

Doc: `STORE_SUBMISSION.md`
