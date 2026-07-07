-- Plano compartilhado / família: grupo doméstico, convites e derivação de plano.

CREATE TABLE households (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    base_meal_plan_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_household_owner FOREIGN KEY (owner_user_id) REFERENCES users (id),
    CONSTRAINT fk_household_base_plan FOREIGN KEY (base_meal_plan_id) REFERENCES meal_plans (id)
);

CREATE TABLE household_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    household_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    joined_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hm_household FOREIGN KEY (household_id) REFERENCES households (id),
    CONSTRAINT fk_hm_user FOREIGN KEY (user_id) REFERENCES users (id),
    UNIQUE KEY uk_household_user (household_id, user_id),
    UNIQUE KEY uk_household_member_user (user_id)
);

CREATE INDEX idx_household_members_household ON household_members (household_id);

CREATE TABLE plan_sharing_invitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    household_id BIGINT NOT NULL,
    inviter_user_id BIGINT NOT NULL,
    invitee_email VARCHAR(255) NOT NULL,
    invitee_name VARCHAR(120) NULL,
    token VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    expires_at DATETIME NOT NULL,
    accepted_user_id BIGINT NULL,
    accepted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_psi_household FOREIGN KEY (household_id) REFERENCES households (id),
    CONSTRAINT fk_psi_inviter FOREIGN KEY (inviter_user_id) REFERENCES users (id),
    CONSTRAINT fk_psi_accepted_user FOREIGN KEY (accepted_user_id) REFERENCES users (id),
    UNIQUE KEY uk_psi_token (token),
    INDEX idx_psi_email_status (invitee_email, status)
);

ALTER TABLE meal_plans
    ADD COLUMN household_id BIGINT NULL,
    ADD COLUMN base_meal_plan_id BIGINT NULL,
    ADD CONSTRAINT fk_meal_plan_household FOREIGN KEY (household_id) REFERENCES households (id),
    ADD CONSTRAINT fk_meal_plan_base FOREIGN KEY (base_meal_plan_id) REFERENCES meal_plans (id);

ALTER TABLE meal_plan_generation_jobs
    ADD COLUMN household_id BIGINT NULL,
    ADD COLUMN shared_from_meal_plan_id BIGINT NULL,
    ADD CONSTRAINT fk_mp_job_household FOREIGN KEY (household_id) REFERENCES households (id),
    ADD CONSTRAINT fk_mp_job_shared_from FOREIGN KEY (shared_from_meal_plan_id) REFERENCES meal_plans (id);
