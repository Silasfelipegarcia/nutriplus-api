# Recuperação Flyway V56/V57 — deploy bloqueado (Railway / produção)

## IntelliJ / MySQL local

### Erro A — checksum mismatch V56

```
Migration checksum mismatch for migration version 56
-> Applied to database : 797521757
-> Resolved locally    : 242365722
```

### Erro B — V1 failed / histórico corrompido

```
Detected failed migration to version 1 (initial nutriplus schema).
Please remove any half-completed changes then run repair to fix the schema history.
```

Causa comum: `flyway:repair` após duplicatas marcou entradas como `type = DELETE`, ou ficou linha com `success = 0` enquanto as tabelas já existem.

### Correção (1 comando)

```bash
cd nutriplus-api
chmod +x scripts/local-dev-flyway-repair.sh
./scripts/local-dev-flyway-repair.sh
```

O script:
1. `mvn clean`
2. Se histórico OK → `flyway:repair` (checksum)
3. Se corrompido → drop `flyway_schema_history`, **baseline V58**, `migrate` (V59+)
4. `flyway:validate`

Depois rode **NutriplusApplication** com profile `local`.

**Banco zerado (sem dados a perder):**

```bash
mysql -uroot -e "DROP DATABASE IF EXISTS nutriplus; CREATE DATABASE nutriplus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
./scripts/local-dev-flyway-repair.sh
```

**Importante:** sempre `mvn clean` antes de subir no IntelliJ se aparecer "Found more than one migration with version 56" (artefatos antigos em `target/`).

---

## Sintoma nos logs (Railway / prod)

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
