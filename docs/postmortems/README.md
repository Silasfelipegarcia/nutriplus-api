# Postmortems — Nutri+

Registro de incidentes e aprendizados reais. Cada documento serve para:

- Não repetir o mesmo erro
- Onboarding de quem entra no time
- Base para posts técnicos (blog/LinkedIn)

## Índice

| Data | Título | Severidade | Tags |
|------|--------|------------|------|
| [2026-07-01](./2026-07-01-prod-latency-regression.md) | API rápida no local, 5–40 s em prod | Alta | performance, railway, cache, mysql |
| [2026-07-01](./2026-07-01-flyway-v57-mysql-syntax.md) | Deploy quebrado: V57 com sintaxe PostgreSQL | Alta | flyway, deploy, mysql |

## Como escrever um novo postmortem

1. Copie [`_template.md`](./_template.md) → `YYYY-MM-DD-titulo-curto.md`
2. Preencha com fatos medidos (números, timestamps, commits)
3. Separe **causa raiz** de **contribuintes** (cold start ≠ query lenta)
4. Liste ações **feitas** e **pendentes**
5. Adicione uma linha na tabela acima

## Princípio

> Local rápido não prova prod saudável. Toda mudança Tier S exige pensar em latência do cliente.

Regras do Cursor: `.cursor/rules/api-latency-guardrails.mdc`
