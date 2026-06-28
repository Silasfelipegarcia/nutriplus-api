# Nutri+ — Precificação

Referência de **tiers**, **faixas** e **defaults técnicos**. Valores comerciais podem ser ajustados na tabela `subscription_plan_catalog` via admin sem redeploy.

> **Modelo vigente:** Opção A — trial 7 dias → Essencial R$ 19,90 → Atleta R$ 29,90 (upgrade).

---

## 1. Mapa de tiers B2C

| Tier | Preço | O que inclui |
|------|-------|--------------|
| **Grátis** | R$ 0 | Onboarding, **1 geração de plano/mês** (com cobrança ativa), check-ins, evolução |
| **Essencial** | **R$ 19,90/mês** ou **R$ 179/ano** | Plano IA, check-ins, evolução, lista de compras, **1 regeneração/mês** |
| **Atleta** | **R$ 29,90/mês** ou **R$ 269/ano** | Tudo do Essencial + modo atleta, treinos MET, **regenerações ilimitadas** |
| **Trial** | 7 dias grátis | Acesso **completo** (Essencial + Atleta); exige cartão; converte para Essencial |

| Tier | Público | Monetização |
|------|---------|-------------|
| **Consulta nutricionista** | Paciente → Nutri | R$ 49 – R$ 149 (nutri escolhe) |
| **Nutri+ Pro** | Nutricionista | Taxa ~15% sobre consulta |

---

## 2. Defaults técnicos (catálogo)

Fonte: migration `V45__essential_tier_pricing.sql` + `subscription_plan_catalog`.

| Plano | `price_cents` | Período |
|-------|---------------|---------|
| FREE | 0 | — |
| ESSENTIAL_MONTHLY | 1990 | 30 dias |
| ESSENTIAL_YEARLY | 17900 | 365 dias (~25% off) |
| ATHLETE_MONTHLY | 2990 | 30 dias |
| ATHLETE_YEARLY | 26900 | 365 dias (~25% off) |

Trial: `POST /payments/trial` → 7 dias → cobrança automática **Essencial Mensal** se cartão válido.

Limites de geração: `MealPlanGenerationQuotaService` (1/mês grátis e essencial; ilimitado atleta/trial).

---

## 3. Marketplace nutricionista

Fonte: `pricing_guidelines` (V17).

| Parâmetro | Default |
|-----------|---------|
| Consulta mín. | R$ 49 |
| Consulta máx. | R$ 149 |
| Sugerido | R$ 79 |
| Taxa plataforma | 15% |

---

## 4. Upgrade proporcional

| De | Para | Cobrança |
|----|------|----------|
| Essencial Mensal | Atleta Mensal | Diferença × dias restantes / 30 |
| Essencial Anual | Atleta Anual | Diferença × dias restantes / 365 |
| Atleta Mensal | Atleta Anual | Diferença × dias restantes / 30 |
| Essencial Mensal | Essencial Anual | Diferença × dias restantes / 30 |

Implementação: `SubscriptionService.ehUpgradeProporcional()` + `calcularValorCobranca()`.

---

## 5. Comparação posicionamento

```
Consulta privada tradicional:     R$ 150 – 400  ████████████████████
Nutri+ Pro (marketplace):         R$  49 – 149  ████████
Nutri+ Atleta:                    R$ ~30/mês    ██
Nutri+ Essencial:                 R$ ~20/mês    █
Grátis (1 plano/mês):             R$   0         ▌
```

**Estratégia:** grátis limitado captura curiosos sem queimar token; Essencial monetiza uso real; Atleta monetiza esportistas; marketplace monetiza humano.

---

## 6. Beta vs produção

| Flag | Comportamento |
|------|---------------|
| `SUBSCRIPTION_BILLING` **off** | Tudo liberado (sem paywall, sem limite de geração) |
| `SUBSCRIPTION_BILLING` **on** | Limites e paywall conforme tier |

---

## 7. Variáveis de ambiente

```bash
MERCADOPAGO_ATHLETE_MONTHLY_PRICE_CENTS=2990   # fallback se catálogo vazio
MERCADOPAGO_ATHLETE_YEARLY_PRICE_CENTS=26900
```

Preços canônicos: tabela `subscription_plan_catalog` + admin `/admin/subscription-plans`.

Detalhes de billing: [SUBSCRIPTIONS.md](./SUBSCRIPTIONS.md).
