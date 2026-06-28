# Deploy — Nutri+ API (Railway)

A API Java é implantada no [Railway](https://railway.app) via Dockerfile. Cada branch permanente (`develop`, `homolog`, `main`) aponta para um **service** ou **environment** distinto.

## Pré-requisitos

- Conta Railway com projeto Nutri+
- MySQL provisionado (Railway MySQL plugin ou externo)
- Agente Python (`nutriplus-agentes`) já deployado no mesmo ambiente lógico

## Configuração do serviço

1. Conecte o repositório `nutriplus-api` ao Railway.
2. O arquivo `railway.toml` na raiz define build Docker e health check em `/actuator/health/liveness`.
3. Associe cada branch à environment correspondente:
   - `develop` → ambiente **dev**
   - `homolog` → ambiente **homolog**
   - `main` → ambiente **prod**

## Variáveis de ambiente

### Comuns a todos os ambientes

| Variável | Descrição |
|----------|-----------|
| `SPRING_PROFILES_ACTIVE` | `homolog` (homolog) ou `prod` (prod/dev remoto) |
| `DB_URL` | JDBC MySQL (`jdbc:mysql://host:3306/nutriplus?...`) |
| `DB_USERNAME` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Base64, mínimo 32 bytes decodificados — **obrigatório** |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas (ex.: `https://app.nutriplus.example.com`) |
| `AI_AGENT_URL` | URL interna/pública do agente Python |
| `SERVER_PORT` | `8080` (Railway injeta `PORT`; mapeie se necessário) |

### New Relic (opcional — mesma conta/license do `lupa-cnpj-api`)

Padrão idêntico ao **lupa-cnpj-api** (mesmo repositório/org): agente Java no Docker via `docker-entrypoint.sh`; ativa só com `NEW_RELIC_LICENSE_KEY`.

| Variável | Descrição |
|----------|-----------|
| `NEW_RELIC_LICENSE_KEY` | License key — [one.newrelic.com](https://one.newrelic.com) → API Keys (mesma key do Lupa) |
| `NEW_RELIC_APP_NAME` | Nome do app no painel NR (por ambiente, ver tabela) |
| `NEW_RELIC_ENVIRONMENT` | Opcional — `development`, `staging` ou `production` |

| Branch / ambiente | `NEW_RELIC_APP_NAME` | `NEW_RELIC_ENVIRONMENT` |
|-------------------|----------------------|-------------------------|
| `develop` | `nutriplus-api-dev` | `development` |
| `homolog` | `nutriplus-api-homolog` | `staging` |
| `main` (prod) | `nutriplus-api-prod` | `production` |

O agent (v9.3.0) lê as variáveis de ambiente automaticamente — sem flags `-D` no JVM. Localmente, omita as variáveis para rodar sem NR.

Monitoramento de MySQL no servidor: ver [`docs/observability/NEW_RELIC.md`](./observability/NEW_RELIC.md).

### Por ambiente

#### Dev (`develop`)

```bash
SPRING_PROFILES_ACTIVE=homolog
# ou local remoto com profile homolog — mesmo rigor de secrets que staging
DB_URL=jdbc:mysql://...
JWT_SECRET=<secret-dev>
CORS_ALLOWED_ORIGINS=https://dev.nutriplus.example.com,http://localhost:3000
AI_AGENT_URL=https://agent-dev.up.railway.app
```

#### Homolog (`homolog`)

```bash
SPRING_PROFILES_ACTIVE=homolog
DB_URL=jdbc:mysql://...
JWT_SECRET=<secret-homolog>
CORS_ALLOWED_ORIGINS=https://homolog.nutriplus.example.com
AI_AGENT_URL=https://agent-homolog.up.railway.app
```

O profile `homolog` habilita rate limit e exige secrets via env (sem fallback no repositório).

#### Produção (`main`)

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://...
JWT_SECRET=<secret-prod>
# CORS opcional — padrão em application-prod.properties já inclui Vercel + nutriplus.com.br + nutriplus.app.br
# CORS_ALLOWED_ORIGINS=https://nutriplus.app.br,https://www.nutriplus.app.br,https://nutriplus-web-ten.vercel.app
AI_AGENT_URL=http://nutriplus-agentes.railway.internal:8000
```

### Cache (habilitado por padrão em prod/homolog)

```bash
# Já ativo via application-prod.properties / application-homolog.properties
# CACHE_ENABLED=true

# Desligar se necessário:
# CACHE_ENABLED=false

# Fase 2 — Redis para multi-réplica
CACHE_ENABLED=true
spring.data.redis.host=<redis-host>
spring.data.redis.port=6379
```

Ver matriz completa em `docs/PERFORMANCE.md`.

## Health checks

| Endpoint | Uso |
|----------|-----|
| `GET /actuator/health/liveness` | Liveness (Railway) |
| `GET /actuator/health/readiness` | Readiness (DB + agente IA) |

## Flyway

Migrations rodam automaticamente no boot. Garanta que o banco de cada ambiente esteja vazio ou compatível antes do primeiro deploy.

## Rollback

Reverta o merge na branch e faça redeploy, ou use o histórico de deployments do Railway para voltar à imagem anterior.
