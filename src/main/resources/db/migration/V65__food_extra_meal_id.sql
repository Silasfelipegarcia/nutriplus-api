ALTER TABLE daily_food_extras
    ADD COLUMN meal_id BIGINT NULL;

ALTER TABLE daily_food_extras
    ADD CONSTRAINT fk_food_extra_meal
        FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE SET NULL;

CREATE INDEX idx_food_extra_user_meal ON daily_food_extras (user_id, meal_id);
