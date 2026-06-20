ALTER TABLE nutrition_profiles
    ADD COLUMN health_conditions TEXT NULL,
    ADD COLUMN medications TEXT NULL,
    ADD COLUMN allergies TEXT NULL,
    ADD COLUMN health_notes TEXT NULL;

ALTER TABLE meal_plans
    ADD COLUMN medical_review_status VARCHAR(20) NULL,
    ADD COLUMN medical_review_notes TEXT NULL;
