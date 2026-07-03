# Nutri+ — Checklist de compliance

Use após cada release que toque em IA, dados de saúde ou textos legais.

## Produto

- [ ] Termos e Política de Privacidade com versão atual (`nutriplus.legal.version` = `2026-06-2`)
- [ ] Aceite triplo no onboarding (termos + privacidade + elegibilidade de saúde)
- [ ] Questionário de elegibilidade (gravidez, TCA, renal) antes das condições clínicas
- [ ] Bloqueio server-side de `POST /meal-plans/generate` quando `ai_plan_eligible=false`
- [ ] Registro auditável em `user_legal_acceptances`
- [ ] Links legais no Perfil
- [ ] Exclusão de conta funcional (`DELETE /users/me`)
- [ ] Congelamento de conta (`POST /users/me/freeze`) e reativação documentados — ver [ACCOUNT_LIFECYCLE.md](./ACCOUNT_LIFECYCLE.md)
- [ ] Purge automático após 90 dias congelada (`AccountPurgeScheduler`)
- [ ] Disclaimer visível no plano alimentar
- [ ] `NutriAiDisclaimer` em evolução, reavaliação e treinos
- [ ] Linguagem de IA: "indício sugerido", "estimativa", sem "diagnóstico"

## Agente

- [ ] Prompts Luna/Bruno proíbem diagnóstico clínico
- [ ] `progress_analyzer` usa linguagem sugestiva

## Lojas

- [ ] URL pública da privacidade (GitHub Pages)
- [ ] Categoria Health & Fitness
- [ ] App Privacy / Data safety preenchidos
- [ ] Screenshots sem promessas médicas

## Jurídico (antes do lançamento comercial)

- [ ] Preencher `[PREENCHER]` nos documentos legais
- [ ] Revisão por advogado (LGPD + consumidor)
