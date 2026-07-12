---
name: nutriplus-account-lifecycle
description: >-
  Ciclo de vida de conta Nutri+: congelar, reativar, purge 90 dias, hard delete.
  Use ao implementar POST /users/me/freeze, reativação, login bloqueado, purge
  scheduler ou exclusão de conta — só portal web para freeze/delete.
---

# Nutri+ — Ciclo de Vida da Conta

## Ações

| Ação | Endpoint | Reversível |
|------|----------|------------|
| Congelar | `POST /users/me/freeze` | Sim (reativar) |
| Reativar | `POST /auth/reactivate-account` | — |
| Purge | Job 90 dias após freeze | Não |
| Hard delete | `DELETE /users/me` | Não |

Docs: `ACCOUNT_LIFECYCLE.md` · `RULE-ACCT-*`

## Restrições de canal

**Congelar e hard delete:** só via **portal web** (`WebPortalClientVerifier`) — não no app nativo diretamente.

## Congelar (`FreezeAccountUseCase`)

1. Valida não já congelada
2. Bloqueia **ADMIN**
3. Bloqueia nutricionista com care **ACTIVE** (`RULE-ACCT-004`)
4. Confirma senha + e-mail
5. Cancela renovação MP silenciosamente
6. `account_frozen_at = now()`, `loginEnabled = false`

## Login congelado

`LoginAccessPolicy.FROZEN_MESSAGE` — dados preservados, reativar quando quiser.

## Purge (`RULE-ACCT-006`)

`AccountPurgeScheduler` — **90 dias** após `account_frozen_at` → exclusão definitiva.

## Hard delete

Mesmo contrato de confirmação (senha + e-mail); portal web obrigatório.

## Checklist

- [ ] `WebPortalClientVerifier` em freeze/delete
- [ ] Mensagens de login específicas (frozen vs rejected)
- [ ] Audit `ACCOUNT_FROZEN`
- [ ] MP cancelamento silencioso
- [ ] Não permitir freeze com care ACTIVE (nutri)

## Compliance

Ver também `nutriplus-compliance` — LGPD, `user_legal_acceptances`.
