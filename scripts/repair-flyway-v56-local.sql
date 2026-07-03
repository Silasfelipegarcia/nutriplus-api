-- Reparo local: checksum mismatch na V56 (IntelliJ / MySQL nutriplus)
-- Erro típico:
--   Applied to database : 797521757
--   Resolved locally    : 242365722
--
-- Causa: flyway_schema_history registrou V56 antiga (ex.: nutritionist) e o repo
--        hoje tem V56__performance_indexes.sql.
--
-- Preferir: ./scripts/local-dev-flyway-repair.sh (mvn flyway:repair)

USE nutriplus;

SELECT version, description, script, checksum, success, installed_on
FROM flyway_schema_history
WHERE version = '56';

-- Opção manual (mesmo efeito do flyway:repair para checksum):
-- UPDATE flyway_schema_history
-- SET checksum = 242365722,
--     script = 'V56__performance_indexes.sql',
--     description = 'performance indexes'
-- WHERE version = '56';

SELECT version, checksum, success FROM flyway_schema_history WHERE version = '56';
