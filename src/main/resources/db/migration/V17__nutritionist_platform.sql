-- V17: Nutri+ Pro — plataforma nutricionista

ALTER TABLE users
    ADD COLUMN role ENUM('PATIENT', 'NUTRITIONIST', 'ADMIN') NOT NULL DEFAULT 'PATIENT' AFTER email;

UPDATE users SET role = 'PATIENT';

CREATE TABLE pricing_guidelines (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    min_consultation_price_cents INT NOT NULL DEFAULT 4900,
    max_consultation_price_cents INT NOT NULL DEFAULT 14900,
    suggested_price_cents        INT NOT NULL DEFAULT 7900,
    platform_fee_percent         DECIMAL(5, 2) NOT NULL DEFAULT 15.00,
    care_duration_days_default   INT NOT NULL DEFAULT 30,
    updated_at                   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO pricing_guidelines (min_consultation_price_cents, max_consultation_price_cents, suggested_price_cents)
VALUES (4900, 14900, 7900);

CREATE TABLE nutritionists (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                     BIGINT NOT NULL UNIQUE,
    crn                         VARCHAR(20) NOT NULL,
    bio                         TEXT,
    specialties                 VARCHAR(500),
    consultation_price_cents    INT NOT NULL DEFAULT 7900,
    care_duration_days          INT NOT NULL DEFAULT 30,
    stripe_account_id           VARCHAR(255),
    stripe_onboarding_complete  TINYINT(1) NOT NULL DEFAULT 0,
    marketplace_visible         TINYINT(1) NOT NULL DEFAULT 1,
    crn_verified                TINYINT(1) NOT NULL DEFAULT 0,
    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_nutritionist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_nutritionists_marketplace ON nutritionists(marketplace_visible, crn_verified);

CREATE TABLE nutritionist_invites (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nutritionist_id BIGINT NOT NULL,
    code            VARCHAR(32) NOT NULL UNIQUE,
    max_uses        INT,
    use_count       INT NOT NULL DEFAULT 0,
    expires_at      TIMESTAMP NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invite_nutritionist FOREIGN KEY (nutritionist_id) REFERENCES nutritionists(id) ON DELETE CASCADE
);

CREATE TABLE care_relationships (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id      BIGINT NOT NULL,
    nutritionist_id BIGINT NOT NULL,
    status          ENUM('PRE_ENGAGED', 'PENDING_PAYMENT', 'ACTIVE', 'EXPIRED', 'CANCELLED') NOT NULL DEFAULT 'PRE_ENGAGED',
    source          ENUM('MARKETPLACE', 'INVITE') NOT NULL,
    started_at      TIMESTAMP NULL,
    expires_at      TIMESTAMP NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_care_patient FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_care_nutritionist FOREIGN KEY (nutritionist_id) REFERENCES nutritionists(id) ON DELETE CASCADE,
    CONSTRAINT uq_care_patient_nutritionist UNIQUE (patient_id, nutritionist_id)
);

CREATE INDEX idx_care_nutritionist_status ON care_relationships(nutritionist_id, status);
CREATE INDEX idx_care_patient_status ON care_relationships(patient_id, status);

CREATE TABLE patient_data_consents (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id      BIGINT NOT NULL,
    nutritionist_id BIGINT NOT NULL,
    care_relationship_id BIGINT NOT NULL,
    scopes          VARCHAR(500) NOT NULL DEFAULT 'PROFILE,MEASUREMENTS,MEAL_PLANS,CHECKINS,PROGRESS',
    granted_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_consent_patient FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_consent_nutritionist FOREIGN KEY (nutritionist_id) REFERENCES nutritionists(id) ON DELETE CASCADE,
    CONSTRAINT fk_consent_care FOREIGN KEY (care_relationship_id) REFERENCES care_relationships(id) ON DELETE CASCADE
);

CREATE TABLE consultations (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    care_relationship_id    BIGINT NOT NULL,
    amount_cents            INT NOT NULL,
    platform_fee_cents      INT NOT NULL DEFAULT 0,
    stripe_payment_intent_id VARCHAR(255),
    status                  ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    paid_at                 TIMESTAMP NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_consultation_care FOREIGN KEY (care_relationship_id) REFERENCES care_relationships(id) ON DELETE CASCADE
);

CREATE INDEX idx_consultations_paid ON consultations(status, paid_at);

CREATE TABLE stripe_customers (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL UNIQUE,
    stripe_customer_id  VARCHAR(255) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stripe_customer_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE conversation_threads (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    care_relationship_id    BIGINT NOT NULL UNIQUE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_thread_care FOREIGN KEY (care_relationship_id) REFERENCES care_relationships(id) ON DELETE CASCADE
);

CREATE TABLE messages (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id   BIGINT NOT NULL,
    sender_id   BIGINT NOT NULL,
    body        TEXT NOT NULL,
    read_at     TIMESTAMP NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_thread FOREIGN KEY (thread_id) REFERENCES conversation_threads(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_thread_created ON messages(thread_id, created_at);

CREATE TABLE plan_revisions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    meal_plan_id    BIGINT NOT NULL,
    nutritionist_id BIGINT NOT NULL,
    changes_json    JSON,
    published_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_revision_plan FOREIGN KEY (meal_plan_id) REFERENCES meal_plans(id) ON DELETE CASCADE,
    CONSTRAINT fk_revision_nutritionist FOREIGN KEY (nutritionist_id) REFERENCES nutritionists(id) ON DELETE CASCADE
);

ALTER TABLE meal_plans
    ADD COLUMN plan_source ENUM('AI_ONLY', 'NUTRITIONIST_APPROVED', 'NUTRITIONIST_AUTHORED') NOT NULL DEFAULT 'AI_ONLY' AFTER medical_review_notes,
    ADD COLUMN nutritionist_id BIGINT NULL AFTER plan_source,
    ADD CONSTRAINT fk_meal_plan_nutritionist FOREIGN KEY (nutritionist_id) REFERENCES nutritionists(id) ON DELETE SET NULL;
