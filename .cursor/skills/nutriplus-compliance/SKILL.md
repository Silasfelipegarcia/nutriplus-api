---
name: nutriplus-compliance
description: >-
  Compliance Nutri+ (LGPD, termos legais, elegibilidade saúde, disclaimers IA,
  exclusão de conta). Use ao alterar textos legais, aceites, bloqueios clínicos,
  copy de IA, store submission ou fluxos de privacidade.
---

# Nutri+ — Compliance

## Quando usar

Release que toca: IA, dados de saúde, textos legais, exclusão de conta, lojas.

Checklist fonte: `COMPLIANCE.md` · `legal/AI_DISCLOSURE.md`

## Produto — obrigatório

- [ ] Termos + Privacidade com versão (`nutriplus.legal.version` = `2026-06-2`)
- [ ] Aceite **triplo** onboarding: termos + privacidade + elegibilidade saúde
- [ ] Questionário elegibilidade **antes** de condições clínicas
- [ ] Bloqueio server-side generate se `ai_plan_eligible=false`
- [ ] Registro em `user_legal_acceptances`
- [ ] Links legais no Perfil
- [ ] `DELETE /users/me` e freeze documentados
- [ ] Purge 90 dias (`AccountPurgeScheduler`)
- [ ] Disclaimer visível no plano
- [ ] `NutriAiDisclaimer` em evolução, reavaliação, treinos

## Linguagem IA

- "indício sugerido", "estimativa" — **nunca** "diagnóstico"
- Prompts Luna/Bruno proíbem diagnóstico clínico
- `progress_analyzer` linguagem sugestiva

## Agente

Regras críticas em **código** (`guardrails.py`), não só prompt.

`aiPlanEligible=false` → 422 no generate.

## Lojas

- URL pública privacidade
- Categoria Health & Fitness
- Data safety preenchido
- Screenshots sem promessas médicas

## Jurídico (antes comercial)

- Preencher `[PREENCHER]` nos docs legais
- Revisão advogado LGPD + consumidor

## Skills relacionadas

- `nutriplus-nutrition-domain` — elegibilidade
- `nutriplus-account-lifecycle` — freeze/purge/delete
- `nutriplus-pro-care` — consentimento convite LGPD

## Referência

Versões e campos legais: [reference/legal-versions.md](reference/legal-versions.md)
