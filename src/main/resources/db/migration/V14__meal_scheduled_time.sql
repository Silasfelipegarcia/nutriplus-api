ALTER TABLE meals
    ADD COLUMN scheduled_time TIME NULL;

ALTER TABLE nutrition_profiles
    ADD COLUMN wake_time TIME NULL,
    ADD COLUMN sleep_time TIME NULL;
