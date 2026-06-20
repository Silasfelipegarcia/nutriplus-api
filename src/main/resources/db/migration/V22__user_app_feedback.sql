-- In-app product feedback (Likert + open suggestions).
-- Insights queries: see docs/APP_FEEDBACK.md

CREATE TABLE user_app_feedback (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    survey_version          VARCHAR(20) NOT NULL DEFAULT 'v1',
    trigger_context         VARCHAR(64) NOT NULL DEFAULT 'MANUAL',
    ease_of_use             INT NOT NULL,
    meal_plan_quality       INT NOT NULL,
    ai_helpfulness          INT NOT NULL,
    progress_tracking       INT NOT NULL,
    overall_satisfaction    INT NOT NULL,
    improvement_suggestions TEXT NULL,
    app_version             VARCHAR(32) NULL,
    platform                VARCHAR(16) NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_app_feedback_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_app_feedback_user_created (user_id, created_at DESC)
);
