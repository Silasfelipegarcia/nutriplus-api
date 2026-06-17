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

## Spring Security practices

- Stateless JWT (access + refresh); refresh tokens rejected as access tokens
- BCrypt password hashing
- Rate limiting on `/auth/**` (disable in `test` profile)
- `PasswordMustChangeFilter` for forced password rotation
- CORS from `app.cors.allowed-origins` — no wildcard in production
- Actuator: health/info public; metrics/prometheus authenticated

### Testing security

- Integration tests exercise real JWT flow against Testcontainers MySQL
- `@WebMvcTest` for auth/health controller contracts
- Use `spring-security-test` for method-level checks when added

## Secrets

**Never rely on defaults in production.**

| Secret | Env var | Notes |
|--------|---------|-------|
| JWT signing key | `JWT_SECRET` | Base64, ≥ 32 bytes; **required** in prod |
| Database | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | From managed MySQL |
| AI agent URL | `AI_AGENT_URL` | Internal service URL |

`application-local.properties` dev defaults must not be used when `SPRING_PROFILES_ACTIVE=prod`.

## Container scanning

On push to protected branches, CI builds the Docker image and runs Trivy against the image filesystem.

## Reporting

- CodeQL alerts: GitHub Security tab
- Trivy: workflow logs + SARIF upload when configured
- Dependency Review: PR checks
