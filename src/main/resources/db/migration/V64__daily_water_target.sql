ALTER TABLE nutrition_profiles
    ADD COLUMN daily_water_target_ml INT NULL;

UPDATE nutrition_profiles np
SET daily_water_target_ml = LEAST(
        4000,
        ROUND(np.current_weight_kg * 35 / 50) * 50
            + CASE
                  WHEN np.athlete_mode_enabled = 1
                      AND np.training_daily_extra_kcal IS NOT NULL
                      AND np.training_daily_extra_kcal > 0
                      THEN 500
                  ELSE 0
              END
    )
WHERE np.current_weight_kg IS NOT NULL
  AND np.current_weight_kg > 0
  AND np.severe_renal_restriction = 0;
