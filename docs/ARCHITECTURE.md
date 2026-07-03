# Nutri+ API — Architecture Guide

Evolução incremental para **Clean Architecture** e **12-factor**, inspirado no padrão Hosty.

> **Documentação relacionada:** visão de produto e técnica no padrão C4 → [`C4.md`](./C4.md). Trace, logs e métricas → [`OBSERVABILITY.md`](./OBSERVABILITY.md).  
> **Regra:** ao alterar arquitetura, integrações ou observabilidade, atualize estes três arquivos em conjunto.

## Dependency rule

```
interfaces (REST) → application (use cases) → domain
                        ↓
                 infrastructure (JPA, JWT, HTTP client, …)
```

1. **domain** não importa Spring, JPA ou HTTP.
2. **application** não importa `*Entity`, `*JpaRepository` nem controllers.
3. **controllers** não injetam repositórios JPA diretamente (migrar legado gradualmente).
4. Mapeamento domain ↔ persistence só em `infrastructure.persistence`.

## Package layout

| Package | Responsibility |
|---------|----------------|
| `domain.model` | Records imutáveis (`User`) |
| `domain.entity` | Entidades JPA (legado — migrar para `infrastructure.persistence`) |
| `application.auth` | Login, refresh, register |
| `application.user` | Perfil, troca de senha |
| `application.port` | Ports de saída (`TokenPort`, `UserQueryPort`, …) |
| `application.shared` | Validators, thumbnails, `ActingUserResolver` |
| `infrastructure.persistence` | Adapters JPA |
| `infrastructure.security` | JWT, filters |
| `controller` | REST (migrar para `interfaces.rest` quando estável) |

## Naming

| Artifact | Pattern | Example |
|----------|---------|---------|
| Domain model | `{Noun}` record | `User` |
| JPA entity | `{Noun}` em `domain.entity` | `User` (legado) |
| HTTP DTO | `{Noun}Request` / `{Noun}Response` record | `UserResponse` |
| Use case | `{Verb}{Noun}UseCase` | `UpdateUserProfileUseCase` |
| Port | `{Noun}{Action}Port` | `UserQueryPort` |

## Bounded contexts

- **auth** — registro, login, refresh, lockout, forgot/reset password, reactivate frozen
- **user** — perfil, foto, senha, aceite termos, exclusão conta, **congelamento** (freeze/purge)
- **nutrition** — perfil nutricional, onboarding (`NutritionProfileService`)
- **mealplan** — planos alimentares, listas de compras, geração assíncrona, **PLAN_RESET**, política de regeração
- **progress** — medidas corporais, evolução, reavaliação IA
- **training** — modo atleta, atividades, MET, calorias extras
- **checkin** — check-ins diários, aderência, streak
- **billing** — assinatura atleta (Mercado Pago), trial, catálogo planos
- **marketplace** — busca nutricionistas, ratings, pricing guidelines
- **care** — vínculos paciente-nutri, convites, consultas
- **pro** — portal nutricionista (perfil CRN, pricing, dossiê, relatórios)
- **conversation** — chat Luna/Bruno e care chat
- **admin** — flags, planos, nutricionistas, acessos, e-mail teste
- **analytics** — eventos de produto, feedback in-app
- **legal** — termos, privacidade, consentimento

Legado: muitos contexts ainda em `service/` + `controller/` — migração gradual para `application/`.

## Idempotência

Mutações HTTP usam header `Idempotency-Key` (padrão Stripe). Implementação:

| Camada | Artefato |
|--------|----------|
| Config | `IdempotencyProperties` — env `IDEMPOTENCY_*` |
| Persistência | Flyway `V23__idempotency_keys.sql`, tabela `idempotency_keys` |
| Port | `application/port/IdempotencyStore` |
| Adapter | `infrastructure/persistence/IdempotencyJpaAdapter` |
| HTTP | `infrastructure/web/IdempotencyFilter` |
| Purge | `IdempotencyPurgeJob` (`@Scheduled`) |
| Outbound | `TraceContext` propaga key para `AiAgentClient` |

**Local/dev:** `idempotency.enabled=false` em `application-dev.properties` (mesmo padrão do rate limit).

## Records (Java 21)

- DTOs HTTP e `@ConfigurationProperties` → `record`
- Entidades JPA → classe mutável com getters/setters (nunca `record` em `@Entity`)
