-- V59: garante índices de performance quando V56 falhou parcialmente (retry / FK duplicada).
-- Idempotente: não altera checksum de V56 já aplicada em produção.

SET @db := DATABASE();

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'security_events'
       AND INDEX_NAME = 'idx_security_events_user_created') = 0,
    'CREATE INDEX idx_security_events_user_created ON security_events(user_id, created_at DESC)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'security_events'
       AND INDEX_NAME = 'idx_security_events_user_event_type') = 0,
    'CREATE INDEX idx_security_events_user_event_type ON security_events(user_id, action)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'daily_meal_checkins'
       AND INDEX_NAME = 'idx_checkins_user_date_meal_type') = 0,
    'CREATE INDEX idx_checkins_user_date_meal_type ON daily_meal_checkins(user_id, checkin_date DESC, meal_type)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'meals'
       AND INDEX_NAME = 'idx_meals_meal_plan_id') = 0
    AND (SELECT COUNT(*) FROM information_schema.STATISTICS
         WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'meals'
           AND COLUMN_NAME = 'meal_plan_id' AND SEQ_IN_INDEX = 1) = 0,
    'CREATE INDEX idx_meals_meal_plan_id ON meals(meal_plan_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'progress_reviews'
       AND INDEX_NAME IN ('idx_progress_reviews_user_created', 'idx_progress_review_user_created')) = 0,
    'CREATE INDEX idx_progress_reviews_user_created ON progress_reviews(user_id, created_at DESC)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
