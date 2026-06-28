# Recuperação Flyway V38 — `registration_source` (Railway / produção)

Quando a V38 falha no meio da execução, o Flyway grava uma linha com `success = 0` em `flyway_schema_history` e **bloqueia todo startup** até reparo manual.

## 1. Conectar no MySQL do Railway

No painel Railway → serviço **MySQL** → **Connect** → abra o console SQL (ou use cliente com `MYSQL_URL`).

O schema/database costuma se chamar `railway`.

## 2. Diagnóstico

```sql
USE railway;

SELECT installed_rank, version, description, script, success, installed_on
FROM flyway_schema_history
WHERE version IN ('35', '36', '37', '38')
ORDER BY installed_rank;

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME IN (
    'login_enabled', 'login_enabled_at', 'login_enabled_by', 'registration_source'
  );
```

Anote:

| Situação | `flyway_schema_history` V38 | coluna `registration_source` |
|----------|----------------------------|------------------------------|
| A        | `success = 0`              | não existe                   |
| B        | `success = 0`              | existe                       |
| C        | linha ausente              | não existe                   |
| D        | linha ausente              | existe                       |

## 3. Reparo

### Passo 1 — Remover entrada falha da V38

```sql
DELETE FROM flyway_schema_history
WHERE version = '38' AND success = 0;
```

Se ainda houver linha da V38 com `success = 1` mas a coluna não existir, delete também:

```sql
DELETE FROM flyway_schema_history WHERE version = '38';
```

### Passo 2 — Garantir a coluna (se não existir)

Use **sem** `AFTER` — evita falha se `login_enabled_by` não existir ou estiver em ordem diferente:

```sql
ALTER TABLE users
    ADD COLUMN registration_source VARCHAR(32) NOT NULL DEFAULT 'OPEN';
```

Se der erro *Duplicate column*, a coluna já existe — pule para o passo 3.

### Passo 3 — Marcar V38 como aplicada (coluna já OK)

Só rode se a coluna **já existe** e você removeu a linha da V38 no passo 1 (evita reexecutar o ALTER no redeploy):

```sql
INSERT INTO flyway_schema_history (
    installed_rank, version, description, type, script, checksum,
    installed_by, installed_on, execution_time, success
)
SELECT
    COALESCE(MAX(installed_rank), 0) + 1,
    '38',
    'registration source',
    'SQL',
    'V38__registration_source.sql',
    -1915003087,
    'manual-recovery',
    NOW(),
    0,
    1
FROM flyway_schema_history
HAVING NOT EXISTS (
    SELECT 1 FROM flyway_schema_history h WHERE h.version = '38' AND h.success = 1
);
```

**Checksum `-1915003087`** = conteúdo atual de `V38__registration_source.sql` no repositório.

### Passo 4 — Validar

```sql
SELECT version, success FROM flyway_schema_history WHERE version = '38';

SHOW COLUMNS FROM users LIKE 'registration_source';
```

Esperado: V38 com `success = 1` e coluna `registration_source` presente.

## 4. Redeploy

1. Railway → serviço **nutriplus-api** → **Redeploy**
2. Logs devem mostrar Flyway validate OK e app subindo.
3. Teste: `GET /actuator/health` e login em `https://nutriplus.app.br`

## Cenários rápidos

| Situação | Ação |
|----------|------|
| **A** — falhou, coluna ausente | Passo 1 → Passo 2 → **não** rode Passo 3 → redeploy (Flyway aplica V38) |
| **B** — falhou, coluna existe | Passo 1 → Passo 3 → redeploy |
| **C** — sem linha V38, coluna ausente | Passo 2 → redeploy |
| **D** — sem linha V38, coluna existe | Passo 3 → redeploy |

## Se V35 também não rodou

Se `login_enabled` / `login_enabled_by` não existirem, aplique a V35 antes (ver `V35__user_access_control.sql`) ou restaure backup — a V38 depende da estrutura de acesso da V35.

## Alternativa via CLI (ambiente local apontando pro Railway)

```bash
# Configure MYSQLHOST, MYSQLPORT, MYSQLUSER, MYSQLPASSWORD, MYSQLDATABASE no shell
mvn flyway:repair -Dflyway.url="jdbc:mysql://HOST:PORT/railway?..." -Dflyway.user=... -Dflyway.password=...
```

`repair` remove migrações **failed** do histórico; ainda é necessário garantir a coluna (passo 2) manualmente se o ALTER não completou.
