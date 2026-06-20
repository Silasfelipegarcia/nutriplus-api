ALTER TABLE nutrition_profiles
    ADD COLUMN goal_target_weeks INT NULL;

ALTER TABLE shopping_lists
    ADD COLUMN guidance_json JSON NULL;

ALTER TABLE shopping_list_items
    ADD COLUMN food_type VARCHAR(64) NULL,
    ADD COLUMN protein_leanness VARCHAR(32) NULL,
    ADD COLUMN kcal_estimate INT NULL,
    ADD COLUMN explanation TEXT NULL,
    ADD COLUMN alternatives_json JSON NULL;
