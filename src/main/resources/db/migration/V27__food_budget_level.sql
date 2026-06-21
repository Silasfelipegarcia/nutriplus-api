ALTER TABLE nutrition_profiles
    ADD COLUMN food_budget_level ENUM('ECONOMIC', 'MODERATE', 'FLEXIBLE') NOT NULL DEFAULT 'MODERATE'
        AFTER meal_notes;
