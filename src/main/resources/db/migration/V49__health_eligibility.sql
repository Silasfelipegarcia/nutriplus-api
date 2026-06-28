ALTER TABLE nutrition_profiles
    ADD COLUMN pregnancy_status VARCHAR(20) NOT NULL DEFAULT 'NONE',
    ADD COLUMN eating_disorder_risk BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN severe_renal_restriction BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN ai_plan_eligible BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN ai_plan_ineligible_reason VARCHAR(30),
    ADD COLUMN health_eligibility_ack_at TIMESTAMP,
    ADD COLUMN health_eligibility_version VARCHAR(20);

ALTER TABLE users
    ADD COLUMN privacy_policy_version VARCHAR(20),
    ADD COLUMN health_eligibility_accepted_at TIMESTAMP,
    ADD COLUMN health_eligibility_version VARCHAR(20);

CREATE TABLE user_legal_acceptances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    document_type VARCHAR(30) NOT NULL,
    document_version VARCHAR(20) NOT NULL,
    accepted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    app_platform VARCHAR(30),
    source VARCHAR(30) NOT NULL DEFAULT 'ONBOARDING',
    CONSTRAINT fk_legal_acceptance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_legal_acceptances_user ON user_legal_acceptances(user_id);
