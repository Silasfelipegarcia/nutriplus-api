ALTER TABLE nutrition_profiles
    ADD COLUMN one_time_correction_used_at TIMESTAMP NULL,
    ADD COLUMN last_athlete_regen_at TIMESTAMP NULL,
    ADD COLUMN athlete_regen_eligible TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN plan_regen_locked_until DATE NULL;

ALTER TABLE progress_reviews
    ADD COLUMN physical_discomforts TEXT NULL,
    ADD COLUMN positive_changes TEXT NULL,
    ADD COLUMN general_notes TEXT NULL,
    ADD COLUMN plan_change_suggested TINYINT(1) NULL,
    ADD COLUMN plan_change_rationale TEXT NULL,
    ADD COLUMN keep_plan_message TEXT NULL,
    ADD COLUMN confidence VARCHAR(20) NULL,
    ADD COLUMN plan_regen_consumed TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE meal_plan_generation_jobs
    ADD COLUMN regeneration_reason VARCHAR(40) NULL,
    ADD COLUMN progress_review_id BIGINT NULL,
    ADD CONSTRAINT fk_meal_plan_gen_review FOREIGN KEY (progress_review_id) REFERENCES progress_reviews(id);
