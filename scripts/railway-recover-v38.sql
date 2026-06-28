-- =============================================================================
-- Railway PROD recovery — Flyway V38 failed migration
-- Run in MySQL console: Railway → MySQL plugin → Query
-- Database name is usually: railway
-- =============================================================================

USE railway;

-- --- STEP 0: inspect (read-only) ---
SELECT version, description, success, installed_on
FROM flyway_schema_history
WHERE version = '38';

SELECT COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'registration_source';

-- --- STEP 1: remove failed V38 (REQUIRED — unblocks app startup) ---
DELETE FROM flyway_schema_history
WHERE version = '38' AND success = 0;

-- --- STEP 2: add column if missing ---
-- If query above returned 0 rows, run:
ALTER TABLE users
    ADD COLUMN registration_source VARCHAR(32) NOT NULL DEFAULT 'OPEN';

-- If you get "Duplicate column name", column already exists — skip to STEP 3.

-- --- STEP 3: only if column EXISTS and you deleted V38 in step 1 ---
-- Marks migration as done so redeploy does not re-run ALTER.
-- Checksum matches V38__registration_source.sql in repo (no AFTER clause).
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
    1519232797,
    'manual-recovery',
    NOW(),
    0,
    1
FROM flyway_schema_history
WHERE NOT EXISTS (
    SELECT 1 FROM flyway_schema_history h WHERE h.version = '38' AND h.success = 1
);

-- --- STEP 4: verify ---
SELECT version, success FROM flyway_schema_history WHERE version = '38';
SHOW COLUMNS FROM users LIKE 'registration_source';

-- Then: Railway → nutriplus-api → Redeploy
