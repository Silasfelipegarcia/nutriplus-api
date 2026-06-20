ALTER TABLE nutrition_profiles
    ADD COLUMN athlete_mode_enabled TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN training_daily_extra_kcal DECIMAL(8,2);

CREATE TABLE user_training_activities (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    sport_type          VARCHAR(40) NOT NULL,
    days_per_week       INT NOT NULL,
    minutes_per_session INT NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_training_activity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_training_user (user_id)
);
