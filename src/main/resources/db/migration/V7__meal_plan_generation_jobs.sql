CREATE TABLE meal_plan_generation_jobs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    status          ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    meal_plan_id    BIGINT NULL,
    error_message   TEXT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at      TIMESTAMP NULL,
    completed_at    TIMESTAMP NULL,
    CONSTRAINT fk_mp_job_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_mp_job_plan FOREIGN KEY (meal_plan_id) REFERENCES meal_plans(id) ON DELETE SET NULL
);

CREATE INDEX idx_mp_jobs_user_status ON meal_plan_generation_jobs(user_id, status, created_at DESC);
