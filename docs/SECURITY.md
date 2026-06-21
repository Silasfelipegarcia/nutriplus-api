# Security — Nutri+ API

## Pipeline (without Veracode)

| Tool | Purpose | Workflow |
|------|---------|----------|
| **CodeQL** | SAST for Java | `security.yml` |
| **Dependency Review** | Block risky dependency changes on PRs | `security.yml` |
| **Trivy** | Filesystem scan (`pom.xml`, lockfiles) + container image | `security.yml` |
| **Dependabot** | Automated dependency PRs | GitHub repo settings |
| **Semgrep** | Optional extra SAST | Not enabled by default |

OWASP Dependency-Check is not used in CI (NVD API key / false-positive noise); Trivy covers dependency CVEs without a separate license.

## Threat model (MVP)

| Ativo | Risco | Mitigação |
|-------|-------|-----------|
| JWT / sessão | Roubo de token | HTTPS, refresh separado, lockout login |
| Agente LLM (:8000) | Abuso de custo | `X-Internal-Token`, rede privada, rate limit user |
| Dados de saúde | Vazamento | JWT, logs sem PII, LGPD termos |
| Prompt injection | IA fora de escopo | Delimitadores `<<<USER_DATA>>>`, motor de risco, Evandro |
| Flood HTTP | DDoS / custo | Rate limit IP + user, circuit breaker IA |

## Spring Security practices

- Stateless JWT (access + refresh); refresh tokens rejected as access tokens
- BCrypt password hashing
- Rate limiting: **20/min/IP** (`/auth/**`), **120/min/IP** (geral), **3/h/user** (`POST /meal-plans/generate`), **10/h/user** (`POST /nutrition-profile`)
- `X-Forwarded-For` respeitado quando proxy confiável (`rate-limit.trusted-proxy-ips`)
- `PasswordMustChangeFilter` for forced password rotation
- CORS from `app.cors.allowed-origins` — no wildcard in production
- Actuator: health/info public; prometheus via `X-Metrics-Token` (`METRICS_SCRAPE_TOKEN`)
- Motor de risco: `SecurityRiskService` + tabela `security_events` (injection, 401/429)

### API → Agente (rede privada)

O agente **não** deve ser exposto na internet pública.

| Ambiente | API | Agente |
|----------|-----|--------|
| Local | `:8080` | `:8000`, `REQUIRE_INTERNAL_AUTH=false` |
| Homolog/Prod | Railway private URL | Mesmo VPC/rede; `REQUIRE_INTERNAL_AUTH=true` |

Configure o **mesmo** segredo em ambos:

- API: `AI_AGENT_INTERNAL_TOKEN`
- Agente: `INTERNAL_TOKEN` + `REQUIRE_INTERNAL_AUTH=true`

Header: `X-Internal-Token`

### SQL injection

Somente JPA/JPQL parametrizado. **Proibir** `nativeQuery` sem revisão de segurança.

### Testing security

- Integration tests exercise real JWT flow against Testcontainers MySQL
- `@WebMvcTest` for auth/health/meal-plan controller contracts
- `SecurityRiskServiceTest` — padrões de injection
- Use `spring-security-test` for method-level checks when added

## Secrets

**Never rely on defaults in production.**

| Secret | Env var | Notes |
|--------|---------|-------|
| JWT signing key | `JWT_SECRET` | Base64, ≥ 32 bytes; **required** in prod |
| Database | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | From managed MySQL |
| AI agent URL | `AI_AGENT_URL` | Internal service URL |
| AI internal token | `AI_AGENT_INTERNAL_TOKEN` | Shared with agente |
| Metrics scrape | `METRICS_SCRAPE_TOKEN` | Prometheus `X-Metrics-Token` |

`application-local.properties` dev defaults must not be used when `SPRING_PROFILES_ACTIVE=prod`.

## Container scanning

On push to protected branches, CI builds the Docker image and runs Trivy against the image filesystem.

## Reporting

- CodeQL alerts: GitHub Security tab
- Trivy: workflow logs + SARIF upload when configured
- Dependency Review: PR checks
- Incidentes: `correlationId` nos logs + `SELECT * FROM security_events ORDER BY created_at DESC LIMIT 50`
