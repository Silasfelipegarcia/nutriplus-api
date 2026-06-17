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
CORS_ALLOWED_ORIGINS=https://app.nutriplus.example.com
AI_AGENT_URL=https://agent-prod.up.railway.app
```

## Health checks

| Endpoint | Uso |
|----------|-----|
| `GET /actuator/health/liveness` | Liveness (Railway) |
| `GET /actuator/health/readiness` | Readiness (DB + agente IA) |

## Flyway

Migrations rodam automaticamente no boot. Garanta que o banco de cada ambiente esteja vazio ou compatível antes do primeiro deploy.

## Rollback

Reverta o merge na branch e faça redeploy, ou use o histórico de deployments do Railway para voltar à imagem anterior.
