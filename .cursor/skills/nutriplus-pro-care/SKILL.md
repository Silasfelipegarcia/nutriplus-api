---
name: nutriplus-pro-care
description: >-
  Nutri+ Pro: marketplace, care relationships (PRE_ENGAGED/ACTIVE), nutricionista
  CRN, dossiê, chat, Stripe, convites. Use ao implementar fluxos /pro/*, care,
  consultas, portal nutricionista ou CTAs de nutricionista no app paciente.
---

# Nutri+ — Pro e Care

## Princípio

**IA primeiro, nutricionista opcional.** Nutricionista nunca bloqueia uso da IA.

Docs: `NUTRI_PLUS_PRO.md` · `RULE-PRO-*` em `RULES_MAP.md`

## Papéis

| Role | Acesso |
|------|--------|
| `PATIENT` | App Flutter (default) |
| `NUTRITIONIST` | Portal `/pro/*` (CRN) |
| `ADMIN` | Moderação marketplace |

## Status `care_relationships`

| Status | Dossiê? | Chat? |
|--------|---------|-------|
| `PRE_ENGAGED` | ✅ (com consentimento) | ❌ |
| `PENDING_PAYMENT` | ❌ | ❌ |
| `ACTIVE` | ✅ | ✅ |
| `EXPIRED` | ❌ | ❌ |
| `CANCELLED` | ❌ | ❌ |

Fonte: `INVITE` | `MARKETPLACE`

## Jornadas principais

**Marketplace:** `GET /nutritionists` → `POST /care/request/{id}` → `POST /consultations/pay` → ACTIVE + chat

**Convite:** nutri gera invite → paciente `POST /care/accept-invite/{code}` + LGPD → PRE_ENGAGED → paga consulta → ACTIVE

## Regras críticas

- `RULE-PRO-002`: chat só com care **ACTIVE**
- `RULE-ACCT-004`: nutricionista com care ACTIVE **não pode congelar** conta
- `NUTRITIONIST_BYPASS`: regeração de plano em fluxo Pro ignora locks de produto
- Planos: `AI_ONLY` ou `NUTRITIONIST_APPROVED`

## Repos

| Repo | O quê |
|------|-------|
| `nutriplus-api` | Regras, persistência, Stripe, chat |
| `nutriplus-web` | `src/presentation/pro/` — portal nutricionista |
| `nutriplus-frontend` | CTAs paciente (marketplace, convite) |

## Pagamentos

Consultas: **Stripe** (`RULE-PRO-003`)  
B2C atleta: Mercado Pago (skill separado se mexer em `SUBSCRIPTIONS.md`)

## Checklist

- [ ] Transição de status válida
- [ ] Consentimento LGPD no convite
- [ ] Autorização: paciente só vê seu care; nutri só seus pacientes
- [ ] Chat polling ≥ 8s
- [ ] Dossiê não expõe dados além do necessário

## Referência

Endpoints e Stripe: [reference/pro-endpoints.md](reference/pro-endpoints.md)
