ALTER TABLE nutrition_profiles
    ADD COLUMN calculation_method ENUM('ESTIMATE', 'BIOIMPEDANCE') NOT NULL DEFAULT 'ESTIMATE' AFTER meal_notes,
    ADD COLUMN body_fat_percent DECIMAL(5, 2) NULL AFTER calculation_method,
    ADD COLUMN lean_mass_kg DECIMAL(5, 2) NULL AFTER body_fat_percent,
    ADD COLUMN muscle_mass_kg DECIMAL(5, 2) NULL AFTER lean_mass_kg;
