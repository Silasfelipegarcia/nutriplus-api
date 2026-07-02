ALTER TABLE nutrition_profiles
    ADD COLUMN primary_training_time TIME NULL,
    ADD COLUMN athlete_hunger_json VARCHAR(400) NULL;
