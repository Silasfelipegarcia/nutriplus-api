ALTER TABLE meal_plans
    ADD COLUMN diet_review_notes TEXT NULL,
    ADD COLUMN senior_review_notes TEXT NULL,
    ADD COLUMN diet_review_status VARCHAR(20) NULL,
    ADD COLUMN senior_review_status VARCHAR(20) NULL;
