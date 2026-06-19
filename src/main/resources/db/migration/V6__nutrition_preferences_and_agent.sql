ALTER TABLE nutrition_profiles
    ADD COLUMN agent_persona ENUM('LUNA', 'BRUNO') NOT NULL DEFAULT 'LUNA' AFTER restriction,
    ADD COLUMN food_likes TEXT NULL AFTER agent_persona,
    ADD COLUMN food_dislikes TEXT NULL AFTER food_likes,
    ADD COLUMN meal_notes TEXT NULL AFTER food_dislikes;
