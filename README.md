# Nutri+ — Backend Java

API REST Spring Boot alinhada ao padrão **12-factor** e **hosty-app-api**: OAuth2 Resource Server, Flyway, trace IDs, rate limit e Actuator.

Repositório: **nutriplus-api**

## Documentação

| Doc | Descrição |
|-----|-----------|
| [docs/C4.md](docs/C4.md) | **Modelo C4** — visão produto + técnica (contexto, containers, componentes) |
| [docs/OBSERVABILITY.md](docs/OBSERVABILITY.md) | Trace E2E, logs, métricas, runbook |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Clean Architecture e packages |
| [docs/README.md](docs/README.md) | Índice completo |

## Git Flow e deploy

- [docs/GITFLOW.md](docs/GITFLOW.md) — branches `feature/*` → `develop` → `homolog` → `main`
- [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) — Railway, variáveis por ambiente

## Stack

- Java 21, Spring Boot 3.3.5 (sem Lombok — records + classes JPA explícitas)
- MySQL 8 + Flyway (única fonte de schema)
- JWT access/refresh (OAuth2 Resource Server)
- Micrometer + Prometheus
- Actuator health com liveness/readiness

## Convenção de código (Records)

| Camada | Padrão |
|--------|--------|
| Request/Response API | `record` + Jakarta Validation |
| DTOs do cliente HTTP (agente IA) | `record` (Jackson) |
| Config tipada | `record` + `@ConfigurationProperties` |
| Entidades JPA | Classe mutável + construtor protegido + Builder interno |

**Nunca** usar `record` em `@Entity` — JPA exige mutabilidade.

## Pré-requisitos

- Java 21+, Maven 3.9+
- MySQL local (`127.0.0.1:3306`) — banco `nutriplus`
- Agente Python em `http://localhost:8000` (repositório `nutriplus-agentes`)

## Configuração

```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS nutriplus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

O profile `local,dev` já vem ativo por padrão em `application.properties` (mesmo padrão do Hosty). Para rodar localmente, basta MySQL em `127.0.0.1:3306` — não precisa exportar variáveis no terminal.

Opcional (sobrescreve defaults de `application-local.properties`):

```bash
export DB_PASSWORD=sua_senha
export AI_AGENT_URL="http://localhost:8000"
```

**Homologação:** `SPRING_PROFILES_ACTIVE=homolog` — mesmas variáveis obrigatórias que produção (`JWT_SECRET`, `DB_*`, `CORS_ALLOWED_ORIGINS`, `AI_AGENT_URL`).

**Produção:** `SPRING_PROFILES_ACTIVE=prod` — `JWT_SECRET`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `CORS_ALLOWED_ORIGINS` são **obrigatórios** (sem fallback no repo).

### Flyway

Migrations em `src/main/resources/db/migration/`. Schema é gerenciado **exclusivamente** pelo Flyway — `docs/schema.sql` está deprecated.

```bash
mvn flyway:migrate
```

## Executar

```bash
mvn spring-boot:run
```

### Docker (stack completa)

```bash
docker compose up --build
```

Sobe MySQL + agente + API. Flyway roda no boot da API.

## Headers de trace

Resumo — detalhes em [docs/OBSERVABILITY.md](docs/OBSERVABILITY.md) e [docs/C4.md](docs/C4.md).

## Endpoints

| Método | Path | Auth |
|--------|------|------|
| POST | `/auth/register` | Não |
| POST | `/auth/login` | Não |
| POST | `/auth/refresh` | Não |
| GET | `/users/me` | JWT |
| POST | `/nutrition-profile` | JWT |
| GET | `/nutrition-profile` | JWT |
| POST | `/meal-plans/generate` | JWT |
| GET | `/meal-plans/latest` | JWT |
| GET | `/shopping-list/latest` | JWT |
| GET | `/health` | Não |
| GET | `/actuator/health/liveness` | Não |
| GET | `/actuator/health/readiness` | Não |
| GET | `/actuator/prometheus` | JWT |

## Observabilidade

Ver [docs/OBSERVABILITY.md](docs/OBSERVABILITY.md). Resumo: MDC nos logs, propagação ao agente, `audit_log` / `ai_requests_log`, Prometheus, JSON em `prod`.

## Performance

- Baseline e tiers: [docs/PERFORMANCE.md](docs/PERFORMANCE.md), [docs/PERFORMANCE_BASELINE.md](docs/PERFORMANCE_BASELINE.md)
- Auditoria local/prod: `./perf/run-baseline.sh prod` (requer `PERF_TEST_EMAIL` / `PERF_TEST_PASSWORD` em homolog/prod)
- Admin console: `GET /admin/performance/summary` (latência Tier S ao vivo)
- k6 nightly: `.github/workflows/k6-nightly.yml`

## Rate limit

Filtro em memória — adequado para **instância única**. Para múltiplas réplicas, usar Redis ou rate limit no gateway.

## Arquitetura (3 camadas)

O app **não** fala com o agente diretamente. O fluxo principal de plano alimentar:

```
Flutter (:8080 client) → nutriplus-api (:8080) → nutriplus-agentes (:8000)
```

| Camada | Porta | Papel |
|--------|-------|-------|
| **Frontend** (Flutter) | client → API `8080` | Onboarding, polling de job, exibe plano e compras |
| **API** (Spring Boot) | `8080` | Auth, perfil, job async, persistência MySQL |
| **Agentes** (FastAPI) | `8000` | LLM: macros, geração de plano, guia de compras |

O agente **não chama a API** — só recebe `POST /api/v1/meal-plan/generate` da API via `AI_AGENT_URL`.

`GET /meal-plans/latest` e `GET /shopping-list/latest` retornam **404** quando o usuário ainda não tem plano (não indica rota inexistente).

### Checklist dev local

1. MySQL em `127.0.0.1:3306` + Flyway (incl. migration `V25__shopping_guidance_and_goal_timeline.sql`)
2. **API** — `mvn spring-boot:run` → `http://localhost:8080`
3. **Agente** — no repo `nutriplus-agentes`: `uvicorn app.main:app --reload --port 8000`
4. **Flutter** — `AppEnvironment.apiBaseUrl` apontando para `8080`
5. Login com usuário seed: `teste@nutriplus.local` / `Nutri123!` (profile `local,dev`)
6. Fluxo: onboarding → medidas → `POST /meal-plans/generate` → poll `GET /meal-plans/generation-status` → `GET /meal-plans/latest` = **200**

Verificação rápida:

```bash
curl -s http://localhost:8000/health    # agente
curl -s http://localhost:8080/health    # API
```

Detalhes de testes e k6: [docs/TESTING.md](docs/TESTING.md).

## Repositórios relacionados

| Repositório | Função |
|-------------|--------|
| [nutriplus-agentes](../nutriplus-agentes) | Agente Python (Luna/Bruno, Groq) — porta **8000** |
| [nutriplus-frontend](../nutriplus-frontend) | App Flutter — consome API **8080** |

## Usuário de teste (local/dev)

| Email | Senha |
|-------|-------|
| `teste@nutriplus.local` | `Nutri123!` |

Criado automaticamente pelo `DevDataLoader` — ver [docs/TESTING.md](docs/TESTING.md).
