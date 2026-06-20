CREATE TABLE daily_meal_checkins (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    checkin_date    DATE NOT NULL,
    meal_id         BIGINT NULL,
    meal_type       ENUM('BREAKFAST', 'MORNING_SNACK', 'LUNCH', 'AFTERNOON_SNACK', 'DINNER', 'EVENING_SNACK') NOT NULL,
    status          ENUM('DONE', 'SKIPPED') NOT NULL,
    notes           VARCHAR(500) NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_checkin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_checkin_meal FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE SET NULL,
    CONSTRAINT uq_checkin_user_date_meal UNIQUE (user_id, checkin_date, meal_id)
);

CREATE INDEX idx_checkins_user_date ON daily_meal_checkins(user_id, checkin_date DESC);
