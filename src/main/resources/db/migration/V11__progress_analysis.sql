ALTER TABLE nutrition_profiles
    ADD COLUMN progress_review_interval_days INT NOT NULL DEFAULT 15;

CREATE TABLE body_measurement_sessions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    measured_on         DATE NOT NULL,
    weight_kg           DECIMAL(5,2) NOT NULL,
    body_fat_percent    DECIMAL(5,2),
    muscle_mass_kg      DECIMAL(5,2),
    waist_cm            DECIMAL(5,2),
    hip_cm              DECIMAL(5,2),
    chest_cm            DECIMAL(5,2),
    neck_cm             DECIMAL(5,2),
    arm_right_cm        DECIMAL(5,2),
    arm_left_cm         DECIMAL(5,2),
    thigh_right_cm      DECIMAL(5,2),
    thigh_left_cm       DECIMAL(5,2),
    notes               VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_body_measurement_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_body_measurement_user_date (user_id, measured_on DESC)
);

CREATE TABLE progress_reviews (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    current_session_id      BIGINT NOT NULL,
    previous_session_id     BIGINT,
    status                  ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    trend                   ENUM(
        'FAT_LOSS', 'FAT_GAIN', 'MUSCLE_GAIN', 'RECOMPOSITION',
        'MAINTENANCE', 'PLATEAU', 'INSUFFICIENT_DATA'
    ),
    summary                 TEXT,
    recommendations         TEXT,
    week_adherence_percent  INT,
    error_message           TEXT,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at            TIMESTAMP,
    CONSTRAINT fk_progress_review_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_review_current FOREIGN KEY (current_session_id) REFERENCES body_measurement_sessions(id),
    CONSTRAINT fk_progress_review_previous FOREIGN KEY (previous_session_id) REFERENCES body_measurement_sessions(id),
    INDEX idx_progress_review_user_created (user_id, created_at DESC)
);
