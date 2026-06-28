# Nutri+ — Catálogo de features

Matriz de features por módulo, com paridade entre clientes. Guia de jornadas: [PRODUCT.md](./PRODUCT.md).

**Legenda de status:** `MVP` implementado | `Beta` atrás de feature flag | `Gap` parcial ou ausente em um cliente

---

## Resumo por cliente

| Cliente | Repositório | Escopo |
|---------|-------------|--------|
| App Flutter | `nutriplus-frontend` | Mobile + web; experiência principal |
| Portal web | `nutriplus-web` | Desktop: portal `/app/*`, Pro, admin, marketing |
| API | `nutriplus-api` | Backend único para todos os clientes |
| Agente IA | `nutriplus-agentes` | Serviço interno (não exposto ao usuário) |

---

## Módulos do app Flutter

Base: `lib/src/features/`

### auth

| Item | Flutter | Web | API | Status |
|------|---------|-----|-----|--------|
| Login | `LoginScreen` | `/auth/login` | `POST /auth/login` | MVP |
| Cadastro | `RegisterScreen` | `/auth/cadastro` | `POST /auth/register` | MVP |
| Esqueci senha | `ForgotPasswordScreen` | `/auth/esqueci-senha` | `POST /auth/forgot-password` | MVP |
| Reset senha | `ResetPasswordScreen` | `/auth/redefinir-senha` | `POST /auth/reset-password` | MVP |
| Refresh token | `AuthProvider` | `AuthFacade` | `POST /auth/refresh` | MVP |
| Cadastro nutricionista | — | `/auth/cadastro-nutricionista` | `POST /auth/register/nutritionist` | Web only |

Doc: [SECURITY.md](./SECURITY.md)

---

### onboarding

| Item | Flutter | Web | API | Status |
|------|---------|-----|-----|--------|
| Wizard 13 passos | `OnboardingScreen` + steps | `/onboarding/*` | `POST /nutrition-profile`, `/onboarding/complete` | MVP |
| Modo atleta no onboarding | `OnboardingAthleteSetupScreen` | `/onboarding/training` | `/onboarding/complete` | MVP |
| Edição perfil (editMode) | `OnboardingScreen(editMode: true)` | Portal profile | `POST /nutrition-profile` | MVP |
| Subfluxo saúde (4 passos) | `HealthEditStepId` | — | — | Flutter only |
| Termos | `TermsAcceptanceScreen` | `/onboarding/termos` | `POST /users/me/accept-terms` | MVP |
| Welcome coach | `WelcomeCoachSheet` | — | — | Flutter only |

Doc: [ONBOARDING.md](./ONBOARDING.md)

---

### today (Hoje)

| Item | Flutter | Web | API | Status |
|------|---------|-----|-----|--------|
| Dashboard do dia | `TodayScreen` | `/app/dashboard` | `GET /checkins/today` | MVP |
| Check-in refeição | inline | portal dashboard | `POST /checkins` | MVP |
| Extras fora do plano | `OffPlanFoodSheet` | — | `POST /checkins/extras` | Flutter |
| Balance insight | `TodayBalanceSheet` | — | `POST /checkins/balance-insight` | Flutter |
| Streak / stats | header | — | `GET /checkins/stats` | MVP |
| Gerar plano (CTA) | `PlanGenerationCard` | dashboard | `POST /meal-plans/generate` | MVP |

Doc: [ENGAGEMENT.md](./ENGAGEMENT.md)

---

### meal_plan (Plano)

| Item | Flutter | Web | API | Status |
|------|---------|-----|-----|--------|
| Ver plano latest | `MealPlanScreen` | `/app/plano` | `GET /meal-plans/latest` | MVP |
| Gerar plano (fluxo) | `GenerateMealPlanFlowScreen` | facade web | `POST /meal-plans/generate` | MVP |
| Status geração | `PlanGenerationController` | `MealPlanGenerationFacade` | `GET /meal-plans/generation-status` | MVP |
| Sync metas desatualizadas | banner + CTA | `plan-target-sync.ts` | compara perfil vs plano | MVP |
| Notas revisão médica | `PlanDayHeader` | meal-plan component | campos review no plano | MVP |
| Flexibilidade semanal | `WeeklyFlexibilitySection` | — | via shopping guidance | Flutter |

Doc: [INTEGRATIONS.md](./INTEGRATIONS.md), `nutriplus-agentes/docs/SECONDARY_AGENTS.md`

---

### evolution + progress (Evolução)

| Item | Flutter | Web | API | Status |
|------|---------|-----|-----|--------|
| Tab evolução | `EvolutionTabScreen` | `/app/evolucao` | — | MVP |
| Aderência ao plano | `PlanAdherenceChart` | `plan-adherence.ts` | `GET /checkins/adherence` | MVP |
| Medidas corporais | via progress flow | portal evolution | `POST /progress/measurements` | MVP |
| Reavaliação IA | `ProgressReviewScreen` | progress component | `POST /progress/reviews` | MVP |
| Relatório evolução | `EvolutionScreen` | evolution | `GET /progress/evolution` | MVP |
| Agenda 15 dias | schedule | — | `GET /progress/schedule` | MVP |

Doc: [PROGRESS_ANALYSIS.md](./PROGRESS_ANALYSIS.md)

---

### shopping_list (Compras)

| Item | Flutter | Web | API | Status |
|------|---------|-----|-----|--------|
| Lista latest | `ShoppingListScreen` | `/app/compras` | `GET /shopping-list/latest` | MVP |
| Revisão de trocas IA | `ShoppingSwapReviewScreen` | — | `POST /shopping-list/apply-swaps` | Flutter |
| Guidance / impacto | widgets | — | embedded na lista | MVP |

---

### profile (Perfil)

| Item | Flutter | Web | API | Status |
|------|---------|-----|-----|--------|
| Perfil usuário | `ProfileScreen` | `/app/perfil` | `GET/PUT /users/me` | MVP |
| Resumo nutricional | `ProfileSummaryCard` | profile | `GET /nutrition-profile` | MVP |
| Foto avatar | upload | — | `PUT /users/me` | MVP |
| Excluir conta | — | — | `DELETE /users/me` | API only |
| Hub para sub-features | tiles | portal nav | — | MVP |

---

### subscription (Assinatura)

| Item | Flutter | Web | API | Status |
|------|---------|-----|-----|--------|
| Status assinatura | `SubscriptionScreen` | `/app/assinatura` | `GET /payments/subscription` | MVP |
| Catálogo planos | `PlansScreen` | `PlanCatalogComponent` | `GET /plans` | MVP |
| Checkout | `CheckoutScreen` (WebView) | checkout redirect | `POST /payments/checkout` | MVP |
| Cancelar renovação | botão | portal subscription | `POST /payments/subscription/cancel` | MVP |
| Reativar renovação | **Gap** | portal subscription | `POST /payments/subscription/reactivate` | Gap Flutter |
| Trial | — | card register | `POST /payments/trial` | Web |
| Cadastro cartão | — | `/app/cobranca` | `POST /payments/cards` | Web |
| Cotação upgrade | — | plan catalog | `GET /payments/quote` | Web |

Doc: [SUBSCRIPTIONS.md](./SUBSCRIPTIONS.md)

---

### professional (Nutricionista / care)

| Item | Flutter | Web | API | Status |
|------|---------|-----|-----|--------|
| Hub marketplace | `ProfessionalHubScreen` | `/app/marketplace` | `GET /marketplace/nutritionists` | MVP |
| Detalhe nutri | `NutritionistDetailScreen` | marketplace-detail | `GET /marketplace/nutritionists/{id}` | MVP |
| Aceitar convite | `AcceptInviteScreen` | accept-invite | `POST /care/accept-invite/{code}` | MVP |
| Chat care | `CareChatScreen` | — | `ConversationController` | Flutter |
| Pagar consulta | — | marketplace | `POST /consultations/pay` | Web |

Doc: [NUTRI_PLUS_PRO.md](./NUTRI_PLUS_PRO.md)

---

### training (Modo atleta)

| Item | Flutter | Web | API | Status |
|------|---------|-----|-----|--------|
| Perfil treinos | `TrainingProfileScreen` | `/app/treinos` | `TrainingController` | MVP |
| Adicionar atividade | `AddTrainingActivitySheet` | onboarding-training | `POST /training/*` | MVP |
| Medidas atleta | `AthleteMeasurementsPanel` | — | progress endpoints | Flutter |
| Paywall atleta | flag billing | flag billing | 402 se sem assinatura | Beta |

Doc: [TRAINING_MODE.md](./TRAINING_MODE.md)

---

### help, feedback, legal

| Item | Flutter | Web | API | Status |
|------|---------|-----|-----|--------|
| FAQ | `HelpScreen` | — | copy local | MVP |
| Lembretes | `ReminderSettingsScreen` | — | local notifications | Flutter |
| Feedback Likert | `AppFeedbackSheet` | — | `POST /feedback/app` | MVP |
| Documentos legais | `LegalDocumentScreen` | `/privacidade`, `/termos` | `GET /legal/*` | MVP |

Doc: [HELP_CONTENT.md](./HELP_CONTENT.md), [APP_FEEDBACK.md](./APP_FEEDBACK.md)

---

## Portal Pro (nutricionista)

Repositório: `nutriplus-web/src/presentation/pro/`

| Feature | Rota | API |
|---------|------|-----|
| Dashboard | `/pro/dashboard` | `GET /pro/*` |
| Pacientes | `/pro/pacientes` | care relationships |
| Dossiê | `/pro/dossier/:id` | patient data |
| Chat | `/pro/conversas` | conversations |
| Convites | `/pro/convites` | invites |
| Perfil / pricing | `/pro/perfil` | `PUT /pro/pricing` |
| Relatório receita | — | `/pro/reports/revenue` |

Doc: [NUTRI_PLUS_PRO.md](./NUTRI_PLUS_PRO.md)

---

## Admin

Repositório: `nutriplus-web/src/presentation/admin/`

| Feature | Componente | API |
|---------|------------|-----|
| Overview | `admin-overview` | — |
| Planos assinatura | `admin-plans` | `GET/PATCH /admin/subscription-plans` |
| Feature flags | `admin-flags` | `GET/PATCH /admin/feature-flags` |
| Nutricionistas | `admin-nutritionists` | `AdminNutritionistController` |
| Acessos / admins | `admin-access`, `admin-admins` | `AdminAccessController` |

**Gap:** sem documentação detalhada de endpoints admin (fase 2).

---

## Marketing web

| Rota | Descrição |
|------|-----------|
| `/` | Landing one-page |
| `/planos` | Planos marketing |
| `/beta` | Landing beta signup |
| `/baixar-app` | Redirect mobile para lojas |
| `/privacidade`, `/termos`, `/cookies`, `/seguranca` | Legal |

---

## Gaps conhecidos (prioridade)

| Gap | Clientes afetados |
|-----|-------------------|
| Reativar assinatura | Flutter |
| Chat care | Web (só Flutter) |
| Trial + cartão | Flutter (só Web) |
| Subfluxo edição saúde | Web |
| OpenAPI admin | Todos |
| Regeneração automática de plano após editar perfil | Todos |

---

## Manutenção

Ao adicionar feature:

1. Atualizar esta tabela (módulo, telas, endpoints, status).
2. Atualizar doc do repo do cliente (`frontend/docs/FEATURES.md`, `nutriplus-web/docs/README.md`).
3. Se nova integração externa, atualizar [INTEGRATIONS.md](./INTEGRATIONS.md).
