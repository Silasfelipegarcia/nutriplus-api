# Documentação — Nutri+ API

Índice da documentação deste repositório e da plataforma.

| Documento | Audiência | Conteúdo |
|-----------|-----------|----------|
| [**BUSINESS_MODEL.md**](./BUSINESS_MODEL.md) | Produto + negócio | Modelo B2C/B2B2C, validação, acessibilidade, mercado |
| [**PRICING.md**](./PRICING.md) | Produto + comercial | Tiers (grátis, atleta, nutri), faixas, defaults técnicos |
| [**NUTRI_PLUS_PRO.md**](./NUTRI_PLUS_PRO.md) | Produto + engenharia | Regras do marketplace nutricionista, status, endpoints |
| [**HELP_CONTENT.md**](./HELP_CONTENT.md) | Produto | FAQ in-app (copy canônico) |
| [**ENGAGEMENT.md**](./ENGAGEMENT.md) | Produto + engenharia | Gamificação, lembretes, motivação |
| [**C4.md**](./C4.md) | Produto + engenharia | Modelo C4: contexto, containers, componentes, sequências |
| [**OBSERVABILITY.md**](./OBSERVABILITY.md) | Engenharia / SRE | Trace, logs, métricas, runbook, gaps e roadmap |
| [**ARCHITECTURE.md**](./ARCHITECTURE.md) | Engenharia | Clean Architecture, packages, bounded contexts |
| [**SECURITY.md**](./SECURITY.md) | Engenharia | JWT, rate limit, lockout |
| [**DEPLOYMENT.md**](./DEPLOYMENT.md) | DevOps | Railway, variáveis de ambiente |
| [**GITFLOW.md**](./GITFLOW.md) | Time | Branches e releases |
| [**TESTING.md**](./TESTING.md) | Engenharia | Testes unitários e integração |

### Outros repositórios

| Repositório | Doc principal |
|-------------|---------------|
| `nutriplus-frontend` | `docs/ARCHITECTURE.md` |
| `nutriplus-agentes` | `docs/architecture.md` |
| `nutriplus-pro-web` | `README.md` + [NUTRI_PLUS_PRO.md](../nutriplus-api/docs/NUTRI_PLUS_PRO.md) (API) |

### Manutenção

Ao implementar features ou mudanças transversais:

1. Atualize o doc técnico do repositório alterado.
2. Se afetar integração ou trace, atualize **C4.md** e **OBSERVABILITY.md** nesta API (fonte canônica da plataforma).
