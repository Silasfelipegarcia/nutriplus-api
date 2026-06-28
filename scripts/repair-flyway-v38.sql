-- Flyway V38 recovery — run on Railway MySQL (database: railway)
-- See docs/FLYWAY_V38_RECOVERY.md for full walkthrough.

-- 1) Inspect
SELECT installed_rank, version, description, success, installed_on
FROM flyway_schema_history
WHERE version IN ('35', '38')
ORDER BY installed_rank;

SELECT COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'registration_source';

-- 2) Remove failed V38 (unblocks startup)
DELETE FROM flyway_schema_history
WHERE version = '38' AND success = 0;

-- 3) Add column if missing (safe: no AFTER clause)
-- Run only if step 1 shows no registration_source column:
-- ALTER TABLE users
--     ADD COLUMN registration_source VARCHAR(32) NOT NULL DEFAULT 'OPEN';

-- 4) If column already exists, mark V38 success (skip on redeploy):
-- INSERT INTO flyway_schema_history (
--     installed_rank, version, description, type, script, checksum,
--     installed_by, installed_on, execution_time, success
-- )
-- SELECT
--     COALESCE(MAX(installed_rank), 0) + 1,
--     '38', 'registration source', 'SQL', 'V38__registration_source.sql',
--     1519232797, 'manual-recovery', NOW(), 0, 1
-- FROM flyway_schema_history
-- HAVING NOT EXISTS (
--     SELECT 1 FROM flyway_schema_history h WHERE h.version = '38' AND h.success = 1
-- );
