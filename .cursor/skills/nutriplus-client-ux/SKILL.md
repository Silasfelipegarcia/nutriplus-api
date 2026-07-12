---
name: nutriplus-client-ux
description: >-
  UX de clientes Nutri+ (Flutter e Web): loading Tier A/B/C, jornada Hoje/Plano/
  Compras/Evolução, onboarding 13 passos, check-ins optimistas, geração de plano.
  Use ao implementar telas, fluxos, loading, erros ou paridade app/portal no Nutri+.
---

# Nutri+ — Client UX

## Missão

Entregar experiência **fácil, previsível e sem tela morta** nos clientes Nutri+, alinhada aos SLAs da API.

Combine com skill pessoal `mobile-app-ux` para princípios gerais.

Fonte: `nutriplus-api/docs/CLIENT_LOADING_UX.md` · `PRODUCT.md`

## Jornada principal (4 abas)

```text
Hoje (check-ins) → Plano (cardápio) → Compras (lista) → Evolução (medidas)
```

Cada aba = uma tarefa clara. Não misturar objetivos na mesma tela.

## Tiers de loading

| Tier | Exemplos | Padrão |
|------|----------|--------|
| **A** < 500ms | check-in, toggle | Optimistic UI; spinner só > 300ms |
| **B** 0,5–2s | salvar perfil, elegibilidade | `NutriBusyButton` + disable |
| **C** até ~30s | gerar/zerar plano | Notice na aba Plano; **sem** overlay global |

## Tier C — ordem obrigatória (gerar/zerar)

1. Botão `isBusy = true`
2. `goToPlanTab()` + `popUntil(isFirst)`
3. `await refresh(force)` no `AppDataStore`
4. `requestGeneration` em background
5. `PlanGeneratingNotice` na `MealPlanScreen`
6. `isBusy = false` após navegação (não após job)

**Regra:** usuário vê aba Plano com notice **antes** do job terminar.

## Componentes Flutter

| Componente | Arquivo |
|------------|---------|
| `NutriBusyButton` | `lib/src/core/nutri_busy_button.dart` |
| `NutriAsyncBody` | `lib/src/core/nutri_ui_widgets.dart` |
| `AppDataStore` | `lib/providers/app_data_store.dart` |
| `PlanGenerationController` | `lib/providers/plan_generation_controller.dart` |
| `PlanGeneratingNotice` | `lib/src/widgets/plan_generating_notice.dart` |
| Launcher | `meal_plan_generation_launcher.dart` |

## Entry points Tier C

| Ação | Tela |
|------|------|
| Gerar (vazio) | `PlanGenerationCard` |
| Zerar | `PlanResetFlowScreen`, hub, `MealPlanScreen` |
| Correção única | `PlanCorrectionFlowScreen` |
| Review → plano | `ProgressReviewScreen` |
| Pós-save perfil | `ProfileEditEligibility` |

## Anti-padrões Nutri+ (já causaram bugs)

- Corpo branco na aba Plano após reset (`_plan == null` sem empty state)
- `popUntil` antes do callback de sucesso no reset
- Overlay global bloqueando app inteiro na geração
- Polling < 8s no chat
- Refetch total a cada `Navigator.pop` — usar `AppDataStore`
- > 3 requests no first paint do hub

## Web (portal)

- `Promise.all` ou `/app/bootstrap` no login
- `plan-reset-entry.component` — paridade zerar plano
- Feature flags em `sessionStorage` — não bloquear login

## Onboarding (13 passos)

Wizard Flutter `OnboardingStepId` — levar ao primeiro valor sem parede de campos.

Saúde: `HealthEligibilityScreen` + gate `ai_plan_eligibility_gate.dart` se inelegível.

## QA — critérios de aceite

- [ ] Zerar plano → nunca corpo branco na aba Plano
- [ ] Busy em ≤ 200 ms após tap (Tier B/C)
- [ ] Check-in responde no mesmo frame (Tier A)
- [ ] Falha geração → mensagem + retry, não tela vazia
- [ ] Hub perfil: spinner no tile tocado, não global

## Referência

Mapa de arquivos e sequência: [reference/client-components.md](reference/client-components.md)
