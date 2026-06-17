# Git Flow — Nutri+ API

Estratégia de branches para desenvolvimento, homologação e produção.

## Branches permanentes

| Branch | Ambiente | Deploy |
|--------|----------|--------|
| `develop` | Desenvolvimento | Railway (dev) |
| `homolog` | Homologação / staging | Railway (homolog) |
| `main` | Produção | Railway (prod) |

## Fluxo de trabalho

```
feature/*  →  develop  →  homolog  →  main
```

1. **feature/\*** — trabalho diário a partir de `develop`.
2. **develop** — integração contínua; deploy automático no ambiente de dev.
3. **homolog** — validação de QA/stakeholders antes de produção.
4. **main** — código em produção; apenas merges aprovados de `homolog`.

## Convenções

- Nomear features: `feature/nome-curto` (ex.: `feature/meal-plan-export`).
- Pull requests para `develop` exigem CI verde.
- Promoção `develop` → `homolog` → `main` via PR ou merge direto conforme política do time.
- Hotfixes críticos: branch `hotfix/*` a partir de `main`, merge em `main` e backport para `develop`.

## CI

O workflow `.github/workflows/ci.yml` roda em push/PR para `develop`, `homolog` e `main`.

## Deploy

Variáveis e passos por ambiente: [DEPLOYMENT.md](./DEPLOYMENT.md).
