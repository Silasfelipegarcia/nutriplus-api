# Nutri+ — Precificação

Referência de **tiers**, **faixas** e **defaults técnicos**. Valores comerciais finais podem ser ajustados na tabela `pricing_guidelines` sem redeploy de regras de negócio.

---

## 1. Mapa de tiers

| Tier | Público | Preço (direção) | O que inclui |
|------|---------|-----------------|--------------|
| **Gratuito** | Paciente B2C | R$ 0 | IA (Luna/Bruno), onboarding, plano alimentar IA, check-ins, evolução a cada 15 dias, lista de compras |
| **Atleta** | Paciente B2C | **A definir** — sugestão R$ 19,90–29,90/mês | Modo atleta, treinos + MET, recálculo de macros, plano alinhado ao gasto (ver [TRAINING_MODE.md](./TRAINING_MODE.md)) |
| **Consulta nutricionista** | Paciente → Nutri | **R$ 49 – R$ 149** (nutri escolhe) | X dias de chat + revisão humana do plano (default 30 dias) |
| **Nutri+ Pro (portal)** | Nutricionista | Taxa **% por consulta** paga no app | Caseload, dossiê, convites, chat, relatório financeiro, Stripe Connect |

**Importante:** paciente **não paga assinatura Nutri+** no MVP para falar com nutricionista — paga **consulta avulsa** ao profissional. A Nutri+ cobra o **nutricionista** (intermediação).

---

## 2. Defaults técnicos (MVP implementado)

Fonte: migration `V17__nutritionist_platform.sql` + entidade `PricingGuideline`.

| Parâmetro | Default | Descrição |
|-----------|---------|-----------|
| `min_consultation_price_cents` | **4900** (R$ 49) | Piso da consulta no marketplace |
| `max_consultation_price_cents` | **14900** (R$ 149) | Teto (acessibilidade / baixa renda) |
| `suggested_price_cents` | **7900** (R$ 79) | Destaque no cadastro do nutricionista |
| `platform_fee_percent` | **15%** | Taxa Nutri+ sobre consulta |
| `care_duration_days_default` | **30** | Dias de chat + acompanhamento após pagamento |

API valida preço do nutricionista em `PUT /pro/pricing` — rejeita valores fora da faixa.

---

## 3. Simulação de split (consulta)

Exemplo com consulta a **R$ 79** e taxa **15%**:

| Parte | Valor |
|-------|-------|
| Paciente paga | R$ 79,00 |
| Taxa plataforma (15%) | R$ 11,85 |
| Nutricionista líquido | R$ 67,15 |

Relatório mensal do portal: `/pro/reports/revenue` (bruto, taxa, líquido, ticket médio).

> Percentual final e política de repasse: **decisão comercial pendente** — documentar aqui quando fechado.

---

## 4. Modo atleta — proposta comercial

**Status no produto:** feature existe no app; **paywall ainda não implementado** (regra de negócio aprovada, billing Fase 2).

### Benchmark mercado BR

- Apps fitness/nutri premium: R$ 25–45/mês
- Público classe C digital: sensível acima de R$ 30/mês

### Faixa sugerida Nutri+

| Plano | Preço sugerido | Justificativa |
|-------|----------------|---------------|
| Mensal | **R$ 24,90** | Abaixo de Lifesum/Yazio; ancoragem “menos que 1 lanche” |
| Anual | **R$ 199** (~R$ 16,50/mês) | Retenção; desconto ~33% |

### O que permanece grátis (sem atleta)

- Perfil, metas, plano IA, check-ins, evolução, modo sedentário/leve/moderado/intenso no onboarding
- Contratar nutricionista (paga só a consulta, não assinatura Nutri+)

### O que exige atleta (pago)

- Switch “modo atleta” + múltiplos treinos
- `POST /training/apply` recalculando macros com kcal extra
- (Futuro) integrações Strava / periodização

---

## 5. Comparação posicionamento

```
Preço consulta privada tradicional:     R$ 150 – 400  ████████████████████
Nutri+ Pro (faixa permitida):           R$  49 – 149  ████████
Nutri+ gratuito (IA):                   R$   0         ▌
Assinatura atleta (proposta):           R$ ~25/mês    ██
```

**Estratégia:** gratuito captura quem não pagaria consulta; atleta monetiza esportista; marketplace monetiza quem quer humano a preço popular.

---

## 6. Políticas de preço (regras)

1. Nutricionista **não pode** cobrar abaixo de R$ 49 nem acima de R$ 149 no app (MVP).
2. Plataforma **não fixa** preço único — nutricionista escolhe na faixa (autonomia + teto social).
3. Paciente vê preço **antes** de contratar (`GET /nutritionists/{id}`).
4. Pagamento em modo dev: `STRIPE_MOCK_MODE=true` (sem cartão real).
5. Assinatura atleta: quando implementada, **não substitui** consulta nutri — são produtos separados.

---

## 7. Roadmap de billing

| Fase | Entrega |
|------|---------|
| **MVP (atual)** | Consulta avulsa + taxa % + mock Stripe |
| **Fase 2** | Stripe Billing: assinatura atleta + renovação mensal paciente↔nutri |
| **Fase 3** | Vouchers, preço por região, planos corporativos |

---

## 8. Variáveis de ambiente

```bash
# API — Stripe real (produção)
STRIPE_SECRET_KEY=
STRIPE_WEBHOOK_SECRET=
STRIPE_MOCK_MODE=false

# Ajuste de faixa: via SQL/admin em pricing_guidelines (futuro: endpoint admin)
```
