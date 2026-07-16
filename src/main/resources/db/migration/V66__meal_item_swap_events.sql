CREATE TABLE meal_item_swap_events (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    meal_id         BIGINT NOT NULL,
    meal_item_id    BIGINT NOT NULL,
    from_food_name  VARCHAR(200) NOT NULL,
    to_food_name    VARCHAR(200) NOT NULL,
    from_calories   DECIMAL(8, 2) NULL,
    to_calories     DECIMAL(8, 2) NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_swap_event_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_swap_event_meal FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE,
    INDEX idx_swap_event_user_created (user_id, created_at DESC)
);
