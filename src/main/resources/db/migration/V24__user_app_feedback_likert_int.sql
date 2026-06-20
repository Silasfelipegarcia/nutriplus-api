-- Align Likert columns with JPA int mapping (Hibernate schema validation).

ALTER TABLE user_app_feedback
    MODIFY ease_of_use INT NOT NULL,
    MODIFY meal_plan_quality INT NOT NULL,
    MODIFY ai_helpfulness INT NOT NULL,
    MODIFY progress_tracking INT NOT NULL,
    MODIFY overall_satisfaction INT NOT NULL;
