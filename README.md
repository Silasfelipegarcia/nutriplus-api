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

## Rate limit

Filtro em memória — adequado para **instância única**. Para múltiplas réplicas, usar Redis ou rate limit no gateway.

## Repositórios relacionados

| Repositório | Função |
|-------------|--------|
| [nutriplus-agentes](../nutriplus-agentes) | Agente Python |
| [nutriplus-frontend](../nutriplus-frontend) | App Flutter |
