ALTER TABLE nutrition_profiles
    MODIFY COLUMN calculation_method ENUM('ESTIMATE', 'BIOIMPEDANCE', 'MANUAL_BMR') NOT NULL DEFAULT 'ESTIMATE';

ALTER TABLE nutrition_profiles
    ADD COLUMN manual_bmr_kcal DECIMAL(8, 2) NULL AFTER body_fat_percent;
