# Mercado Pago — Nutri+ (setup)

Integração B2C para assinatura **Modo Atleta**, replicada do lupa-cnpj-api.

## Variáveis (Railway / `.env`)

```bash
MERCADOPAGO_ACCESS_TOKEN=          # test ou prod
MERCADOPAGO_PUBLIC_KEY=            # front web (Vercel: MERCADOPAGO_PUBLIC_KEY)
MERCADOPAGO_WEBHOOK_SECRET=
MERCADOPAGO_MOCK_MODE=true           # dev: ativa plano sem MP
MERCADOPAGO_ATHLETE_MONTHLY_PRICE_CENTS=2490
MERCADOPAGO_ATHLETE_YEARLY_PRICE_CENTS=19900
FRONTEND_URL=https://nutriplus.com.br
API_PUBLIC_URL=https://nutriplus-api-production.up.railway.app
```

## Webhook

Configure no painel Mercado Pago:

```
POST/GET {API_PUBLIC_URL}/payments/mercadopago/webhook
```

Em produção, `MERCADOPAGO_WEBHOOK_SECRET` é obrigatório.

## Fluxo de teste (mock)

1. `MERCADOPAGO_MOCK_MODE=true` na API
2. Web: `/app/planos` → Assinar → redireciona e ativa plano localmente
3. Mobile: Perfil → Assinatura → Ver planos → checkout mock

## Fluxo sandbox MP

1. Credenciais `TEST-...` na API e `MERCADOPAGO_PUBLIC_KEY` no Vercel
2. Cadastrar cartão em `/app/cobranca` (web)
3. Trial: `POST /payments/trial` (exige cartão)
4. Checkout redirect → `/app/planos/sucesso` → sync

## Endpoints

| Método | Rota |
|--------|------|
| GET | `/plans` |
| GET | `/payments/config` |
| POST | `/payments/checkout` |
| POST | `/payments/checkout/sync` |
| POST | `/payments/charge` |
| POST | `/payments/trial` |
| GET | `/payments/subscription` |
| POST | `/payments/subscription/cancel` |
| POST | `/payments/subscription/reactivate` |
| GET/POST | `/payments/mercadopago/webhook` |

## Paywall

- `TrainingService.saveProfile` com `athleteModeEnabled=true` → 402 se sem assinatura/trial/grace **e** feature flag `SUBSCRIPTION_BILLING` ligada
- Usuários com modo atleta ativo antes do deploy recebem **30 dias de grace** (`athlete_grace_until`)

## Beta vs. cobrança

Durante o beta, deixe **desligada** a feature flag `SUBSCRIPTION_BILLING` em `/admin/flags`. O modo atleta fica liberado sem assinatura.

Quando for cobrar:

1. Configure preços/nomes em `/admin/planos`
2. Ligue `SUBSCRIPTION_BILLING` em `/admin/flags`
3. Configure credenciais MP e webhook
