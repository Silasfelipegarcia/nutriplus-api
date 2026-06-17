-- V1: schema inicial Nutri+

CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE nutrition_profiles (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL UNIQUE,
    age                 INT NOT NULL,
    sex                 ENUM('MALE', 'FEMALE') NOT NULL,
    height_cm           DECIMAL(5,2) NOT NULL,
    current_weight_kg   DECIMAL(5,2) NOT NULL,
    target_weight_kg    DECIMAL(5,2) NOT NULL,
    goal                ENUM('LOSE_WEIGHT', 'MAINTAIN_WEIGHT', 'GAIN_MASS') NOT NULL,
    activity_level      ENUM('SEDENTARY', 'LIGHT', 'MODERATE', 'INTENSE') NOT NULL,
    dietary_preference  ENUM('OMNIVORE', 'VEGETARIAN', 'VEGAN') NOT NULL,
    restriction         ENUM('NONE', 'LACTOSE', 'GLUTEN', 'LACTOSE_GLUTEN') NOT NULL DEFAULT 'NONE',
    bmr_kcal            DECIMAL(8,2),
    tdee_kcal           DECIMAL(8,2),
    target_calories     DECIMAL(8,2),
    target_protein_g    DECIMAL(8,2),
    target_carbs_g      DECIMAL(8,2),
    target_fat_g        DECIMAL(8,2),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_nutrition_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE meal_plans (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    nutrition_profile_id BIGINT NOT NULL,
    plan_date           DATE NOT NULL,
    total_calories      DECIMAL(8,2),
    total_protein_g     DECIMAL(8,2),
    total_carbs_g       DECIMAL(8,2),
    total_fat_g         DECIMAL(8,2),
    disclaimer          TEXT NOT NULL,
    ai_model            VARCHAR(100),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_plan_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_plan_profile FOREIGN KEY (nutrition_profile_id) REFERENCES nutrition_profiles(id) ON DELETE CASCADE
);

CREATE INDEX idx_meal_plans_user_created ON meal_plans(user_id, created_at DESC);

CREATE TABLE meals (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    meal_plan_id    BIGINT NOT NULL,
    meal_type       ENUM('BREAKFAST', 'MORNING_SNACK', 'LUNCH', 'AFTERNOON_SNACK', 'DINNER', 'EVENING_SNACK') NOT NULL,
    name            VARCHAR(150) NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_meal_plan FOREIGN KEY (meal_plan_id) REFERENCES meal_plans(id) ON DELETE CASCADE
);

CREATE TABLE meal_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    meal_id         BIGINT NOT NULL,
    food_name       VARCHAR(200) NOT NULL,
    quantity_g      DECIMAL(8,2) NOT NULL,
    calories        DECIMAL(8,2),
    protein_g       DECIMAL(8,2),
    carbs_g         DECIMAL(8,2),
    fat_g           DECIMAL(8,2),
    CONSTRAINT fk_meal_item_meal FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE
);

CREATE TABLE shopping_lists (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    meal_plan_id    BIGINT NOT NULL,
    week_start      DATE NOT NULL,
    week_end        DATE NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shopping_list_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_shopping_list_plan FOREIGN KEY (meal_plan_id) REFERENCES meal_plans(id) ON DELETE CASCADE
);

CREATE INDEX idx_shopping_lists_user_created ON shopping_lists(user_id, created_at DESC);

CREATE TABLE shopping_list_items (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopping_list_id    BIGINT NOT NULL,
    item_name           VARCHAR(200) NOT NULL,
    quantity            VARCHAR(100) NOT NULL,
    category            VARCHAR(100),
    CONSTRAINT fk_shopping_item_list FOREIGN KEY (shopping_list_id) REFERENCES shopping_lists(id) ON DELETE CASCADE
);

CREATE TABLE ai_requests_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT,
    request_type    ENUM('CALCULATE_MACROS', 'GENERATE_MEAL_PLAN', 'GENERATE_SUBSTITUTIONS', 'GENERATE_SHOPPING_LIST') NOT NULL,
    request_payload JSON NOT NULL,
    response_payload JSON,
    status          ENUM('SUCCESS', 'ERROR') NOT NULL,
    error_message   TEXT,
    duration_ms     INT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_ai_log_user_created ON ai_requests_log(user_id, created_at DESC);
