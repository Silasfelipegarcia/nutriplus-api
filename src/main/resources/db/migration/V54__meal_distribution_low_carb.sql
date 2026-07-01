-- Distribuição de refeições, low carb e carbs em comida fora do plano
ALTER TABLE nutrition_profiles
    ADD COLUMN hunger_pattern VARCHAR(20) NOT NULL DEFAULT 'BALANCED',
    ADD COLUMN nutrition_mode VARCHAR(30) NOT NULL DEFAULT 'STANDARD';

ALTER TABLE daily_food_extras
    ADD COLUMN estimated_carbs_g DECIMAL(8, 1) NULL;
