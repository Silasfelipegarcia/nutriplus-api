ALTER TABLE nutrition_profiles
    ADD COLUMN pace_warning VARCHAR(500) NULL,
    ADD COLUMN estimated_weekly_rate_kg DECIMAL(4, 2) NULL;
