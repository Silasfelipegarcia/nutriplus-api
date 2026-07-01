-- V57: perfil enriquecido do nutricionista + portfólio (MySQL).
-- Idempotente: seguro se ALTER rodou antes do CREATE falhar (BIGSERIAL em deploy anterior).

SET @db := DATABASE();

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'nutritionists' AND COLUMN_NAME = 'formation') = 0,
    'ALTER TABLE nutritionists ADD COLUMN formation TEXT',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'nutritionists' AND COLUMN_NAME = 'experience_years') = 0,
    'ALTER TABLE nutritionists ADD COLUMN experience_years INT',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'nutritionists' AND COLUMN_NAME = 'approach') = 0,
    'ALTER TABLE nutritionists ADD COLUMN approach VARCHAR(500)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'nutritionists' AND COLUMN_NAME = 'languages') = 0,
    'ALTER TABLE nutritionists ADD COLUMN languages VARCHAR(128)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS nutritionist_portfolio_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nutritionist_id BIGINT NOT NULL,
    title           VARCHAR(200) NOT NULL,
    summary         TEXT NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_portfolio_nutritionist FOREIGN KEY (nutritionist_id) REFERENCES nutritionists (id) ON DELETE CASCADE
);

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'nutritionist_portfolio_items'
       AND INDEX_NAME = 'idx_portfolio_nutritionist') = 0,
    'CREATE INDEX idx_portfolio_nutritionist ON nutritionist_portfolio_items (nutritionist_id, sort_order)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'meal_plan_generation_jobs'
       AND COLUMN_NAME = 'nutritionist_notes') = 0,
    'ALTER TABLE meal_plan_generation_jobs ADD COLUMN nutritionist_notes TEXT',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
