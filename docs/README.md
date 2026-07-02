# Documentação — Nutri+ Platform

Hub canônico da documentação da plataforma Nutri+. Este repositório (`nutriplus-api/docs/`) é a **fonte de verdade** para produto, negócio, integrações e arquitetura transversal.

---

## Visão geral

| Documento | Audiência | Conteúdo |
|-----------|-----------|----------|
| [**EXECUTIVE_SUMMARY.md**](./EXECUTIVE_SUMMARY.md) | Investidores, parceiros, liderança | Visão de negócio, produto implementado, receita, stack, compliance |
| [**PRODUCT.md**](./PRODUCT.md) | Produto + engenharia | Personas, jornadas end-to-end, regras de negócio |
| [**FEATURES.md**](./FEATURES.md) | Produto + engenharia | Catálogo de features: Flutter vs Web vs API |

---

## Negócio e comercial

| Documento | Audiência | Conteúdo |
|-----------|-----------|----------|
| [**BUSINESS_MODEL.md**](./BUSINESS_MODEL.md) | Produto + negócio | Modelo B2C/B2B2C, validação, acessibilidade, mercado |
| [**PRICING.md**](./PRICING.md) | Produto + comercial | Tiers (grátis, atleta, nutri), faixas, defaults técnicos |
| [**NUTRI_PLUS_PRO.md**](./NUTRI_PLUS_PRO.md) | Produto + engenharia | Marketplace nutricionista, care, chat, endpoints Pro |
| [**BILLING_AND_AUTH_ROADMAP.md**](./BILLING_AND_AUTH_ROADMAP.md) | Produto + engenharia | **Roadmap** futuro: e-mail transacional, paywall completo |

---

## Features (detalhamento)

| Documento | Conteúdo |
|-----------|----------|
| [**SUBSCRIPTIONS.md**](./SUBSCRIPTIONS.md) | Assinatura atleta — **estado implementado** (Mercado Pago, trial, cancelamento) |
| [**ONBOARDING.md**](./ONBOARDING.md) | Sequência de endpoints e payload do onboarding |
| [**METABOLISM_AND_BODY_COMPOSITION.md**](./METABOLISM_AND_BODY_COMPOSITION.md) | Modos de metabolismo vs histórico de % gordura / evolução |
| [**RELEASE_NOTES_2025-07.md**](./RELEASE_NOTES_2025-07.md) | Novidades jul/2025 (negócio + técnico) |
| [**TRAINING_MODE.md**](./TRAINING_MODE.md) | Modo atleta, MET, treinos, calorias extras |
| [**PROGRESS_ANALYSIS.md**](./PROGRESS_ANALYSIS.md) | Medidas corporais, evolução, reavaliação IA |
| [**ENGAGEMENT.md**](./ENGAGEMENT.md) | Gamificação, lembretes, motivação |
| [**HELP_CONTENT.md**](./HELP_CONTENT.md) | FAQ in-app (copy canônico) |
| [**APP_FEEDBACK.md**](./APP_FEEDBACK.md) | Feedback Likert in-app |
| [**PLAN_REGENERATION.md**](./PLAN_REGENERATION.md) | Travas de regeração, flag `UNLIMITED_PLAN_REGEN` |
| [**RELEASE_UX_MINIMALISTA.md**](./RELEASE_UX_MINIMALISTA.md) | **Release jun/2026:** UX minimalista, plano, quantidades, checklist |

---

## Engenharia e arquitetura

| Documento | Conteúdo |
|-----------|----------|
| [**C4.md**](./C4.md) | Modelo C4: contexto, containers, componentes, sequências |
| [**INTEGRATIONS.md**](./INTEGRATIONS.md) | Mapa de integrações externas (Groq, MP, Stripe, analytics) |
| [**ARCHITECTURE.md**](./ARCHITECTURE.md) | Clean Architecture, packages, bounded contexts |
| [**SECURITY.md**](./SECURITY.md) | JWT, rate limit, lockout, threat model |
| [**PERFORMANCE.md**](./PERFORMANCE.md) | SLA, cache, matriz de endpoints |
| [**postmortems/README.md**](./postmortems/README.md) | Incidentes, aprendizados e base para posts |
| [**TESTING.md**](./TESTING.md) | Testes unitários, integração, k6 |

---

## Operações e DevOps

| Documento | Conteúdo |
|-----------|----------|
| [**DEPLOYMENT.md**](./DEPLOYMENT.md) | Railway, variáveis de ambiente, health checks |
| [**OBSERVABILITY.md**](./OBSERVABILITY.md) | Trace, logs, métricas, runbooks |
| [**observability/README.md**](./observability/README.md) | Dashboards Grafana, alertas Prometheus |
| [**observability/NEW_RELIC.md**](./observability/NEW_RELIC.md) | APM New Relic |
| [**GITFLOW.md**](./GITFLOW.md) | Branches e releases |
| [**MERCADOPAGO_SETUP.md**](./MERCADOPAGO_SETUP.md) | Runbook Mercado Pago (assinatura atleta) |
| [**FLYWAY_V38_RECOVERY.md**](./FLYWAY_V38_RECOVERY.md) | Runbook: reparo migration V38 |
| [**FLYWAY_V56_V57_RECOVERY.md**](./FLYWAY_V56_V57_RECOVERY.md) | Runbook: reparo migrations V56/V57 (deploy bloqueado) |
| [**postmortems/**](./postmortems/README.md) | Postmortems e lições de incidentes |

---

## Compliance e lançamento

| Documento | Conteúdo |
|-----------|----------|
| [**COMPLIANCE.md**](./COMPLIANCE.md) | Checklist release: IA, dados de saúde, legal, lojas |
| [**STORE_SUBMISSION.md**](./STORE_SUBMISSION.md) | Kit App Store / Play Store |
| [**legal/README.md**](./legal/README.md) | Publicação GitHub Pages (termos, privacidade) |
| [**legal/TERMS_OF_USE.md**](./legal/TERMS_OF_USE.md) | Termos de uso B2C |
| [**legal/PRIVACY_POLICY.md**](./legal/PRIVACY_POLICY.md) | Política de privacidade (LGPD) |
| [**legal/AI_DISCLOSURE.md**](./legal/AI_DISCLOSURE.md) | Aviso de uso de IA |

> Textos servidos pela API em runtime: `src/main/resources/legal/` (inclui termos do nutricionista e consentimento de dados).

---

## Outros repositórios

| Repositório | Documentação |
|-------------|--------------|
| `nutriplus-frontend` | [`docs/README.md`](../../nutriplus-frontend/docs/README.md) |
| `nutriplus-agentes` | [`docs/README.md`](../../nutriplus-agentes/docs/README.md) |
| `nutriplus-web` | [`docs/README.md`](../../nutriplus-web/docs/README.md) |

---

## Manutenção

Ao implementar features ou mudanças transversais:

1. Atualize [**FEATURES.md**](./FEATURES.md) se afetar escopo de produto ou paridade entre clientes.
2. Atualize o doc técnico do repositório alterado (frontend, agentes, web).
3. Se afetar integração ou trace, atualize [**C4.md**](./C4.md), [**INTEGRATIONS.md**](./INTEGRATIONS.md) e [**OBSERVABILITY.md**](./OBSERVABILITY.md).
4. Se afetar billing, atualize [**SUBSCRIPTIONS.md**](./SUBSCRIPTIONS.md) (estado atual) — **não** confundir com [`BILLING_AND_AUTH_ROADMAP.md`](./BILLING_AND_AUTH_ROADMAP.md) (futuro).
