CREATE TABLE daily_food_extras (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    entry_date          DATE NOT NULL,
    description         VARCHAR(500) NOT NULL,
    estimated_calories  INT NOT NULL,
    impact_message      TEXT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_food_extra_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_food_extra_user_date (user_id, entry_date DESC)
);
