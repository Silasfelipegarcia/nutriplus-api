# Recuperação Flyway V56/V57 — deploy bloqueado (Railway / produção)

## Sintoma nos logs

```
Cannot resolve reference to bean 'jpaSharedEM_entityManagerFactory'
  → securityEventRepository → securityRiskService → riskEvaluationFilter
```

Isso é **efeito cascata**. A causa está no Flyway: migration `V56` ou `V57` com `success = 0` em `flyway_schema_history`, ou checksum inválido.

O Hikari conecta no MySQL, mas o `EntityManagerFactory` não sobe → Tomcat cai.

## 1. Conectar no MySQL do Railway

Painel Railway → serviço **MySQL** → **Connect** → console SQL.

Schema/database: `railway`.

## 2. Diagnóstico

```sql
USE railway;

SELECT installed_rank, version, description, success, installed_on
FROM flyway_schema_history
WHERE version IN ('56', '57')
ORDER BY installed_rank;
```

| Situação | O que fazer |
|----------|-------------|
| V57 `success = 0` | Caso mais comum (BIGSERIAL). Passo 3 + redeploy. |
| V56 `success = 0` | Índice redundante ou retry após falha parcial. Passo 3 + redeploy. |
| V56 `success = 1` + erro de **checksum** | Passo 4 (repair checksum). |
| Nenhuma linha com `success = 0` | Verificar se deploy usa commit `ec6034f` ou posterior. |

## 3. Reparo (entrada falha)

```sql
DELETE FROM flyway_schema_history
WHERE version IN ('56', '57') AND success = 0;
```

Script completo: `scripts/repair-flyway-prod-deploy.sql`

## 4. Checksum mismatch na V56 (só se aparecer no log)

Se a V56 já foi aplicada com sucesso e o arquivo mudou (idempotência), o Flyway reclama de checksum.

Opção A — Maven (com URL/credenciais do Railway):

```bash
mvn flyway:repair \
  -Dflyway.url='jdbc:mysql://HOST:3306/railway?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
  -Dflyway.user=... \
  -Dflyway.password=...
```

Opção B — Flyway CLI `repair` no mesmo banco.

## 5. Redeploy

1. Confirmar que a API usa imagem com `ec6034f` ou mais recente (V57 MySQL + idempotente).
2. Redeploy do serviço API no Railway.
3. Smoke: `GET /actuator/health` → `200`.

## Histórico

- **V56** — índices de performance (`performance_indexes`). Idempotente desde commit pós-`ec6034f`.
- **V57** — perfil nutricionista + portfólio. Corrigida de `BIGSERIAL` (PostgreSQL) para `AUTO_INCREMENT` (MySQL) em `ec6034f`.

## Referências

- `scripts/repair-flyway-prod-deploy.sql`
- `scripts/repair-flyway-v57.sql`
- `docs/postmortems/2026-07-01-flyway-v57-mysql-syntax.md`
- `docs/FLYWAY_V38_RECOVERY.md` (mesmo padrão operacional)
