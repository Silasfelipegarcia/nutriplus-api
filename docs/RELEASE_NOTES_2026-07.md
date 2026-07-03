# Release notes — julho 2026

Novidades técnicas e de produto entregues ou em staging local neste ciclo.

> **Índice:** [docs/README.md](./README.md) · [RULES_MAP.md](./RULES_MAP.md)

---

## Produto

### Zerar plano (`PLAN_RESET`)

- Fluxo **voltar do zero**: apaga tracking da era atual (check-ins, extras, medidas/reviews desde início do plano), gera novo plano, reinicia ciclo de 15 dias.
- **Não** consome correção única.
- Confirmação destrutiva: checkbox + digitar `ZERAR PLANO`.
- **Clientes:** Flutter (`PlanResetFlowScreen`), Web (`plan-reset-entry`), API (`PlanResetService`).

Doc: [PLAN_REGENERATION.md](./PLAN_REGENERATION.md)

### Ciclo de vida da conta (congelar)

- `POST /users/me/freeze` — preserva dados, bloqueia login, cancela renovação MP.
- `POST /auth/reactivate-account` — reativa com novos tokens.
- Purge automático após **90 dias** congelada (`AccountPurgeScheduler`).
- Portal web only (`WebPortalClientVerifier`).

Doc: [ACCOUNT_LIFECYCLE.md](./ACCOUNT_LIFECYCLE.md)

---

## UX / clientes (Flutter)

### Fix tela em branco pós-reset

- `MealPlanScreen` nunca renderiza corpo vazio.
- Sync `AppDataStore.mealPlanRevision` incluindo plano `null`.
- Launcher: navega para aba Plano → `await refresh` → `requestGeneration`.

Doc: [CLIENT_LOADING_UX.md](./CLIENT_LOADING_UX.md)

### Loading / async padronizado

- Novo `NutriBusyButton` (Tier B/C).
- Optimistic check-ins na aba Hoje.
- Tile loading no hub nutricional (corrigir / zerar).
- Prefetch elegibilidade com dialog se > 300 ms.
- Busy em cancelar/reativar assinatura.

---

## API / infra

### New Relic

- `newrelic-api` incluído no fat jar (não `provided`).
- `NewRelicTraceBridge` com modo defensivo (`disabled` flag se agent ausente).
- Fix `ClassNotFoundException` em `CorrelationIdFilter`.

Doc: [observability/NEW_RELIC.md](./observability/NEW_RELIC.md)

### Bootstrap agregado

- `GET /app/bootstrap` — Tier S; user, perfil, plano, checkins, stats, schedule, generation status em 1 request.

Doc: [PERFORMANCE.md](./PERFORMANCE.md)

---

## Documentação (esta release)

Novos documentos canônicos:

| Doc | Conteúdo |
|-----|----------|
| [RULES_MAP.md](./RULES_MAP.md) | Mapa mestre de regras com IDs |
| [ACCOUNT_LIFECYCLE.md](./ACCOUNT_LIFECYCLE.md) | Freeze / reactivate / purge |
| [CLIENT_LOADING_UX.md](./CLIENT_LOADING_UX.md) | Tiers A/B/C, NutriBusyButton |
| [LATENCY_GUARDRAILS.md](./LATENCY_GUARDRAILS.md) | Guardrails consolidados |
| [C4.md](./C4.md) | C4 aprimorado (web container, novos flows) |

---

## Versões de referência

| Repo | Versão / tag |
|------|--------------|
| nutriplus-frontend | v1.1.6+ (PLAN_RESET UI); v1.1.7+ staging (loading UX) |
| nutriplus-api | main (PLAN_RESET, freeze V62, NR fix) |
| nutriplus-web | main (plan-reset portal) |

---

## Teste manual sugerido

1. Zerar plano sem check-ins → progresso → novo plano ou empty claro.
2. Zerar com check-ins → confirmação → sem tela branca.
3. Gerar plano da aba Hoje → busy → Plano com `PlanGeneratingNotice`.
4. Check-in em rede lenta → checkbox imediato.
5. Congelar conta no portal → login bloqueado → reativar.
