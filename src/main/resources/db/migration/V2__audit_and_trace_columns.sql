CREATE TABLE audit_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id  VARCHAR(64) NULL,
    user_id         BIGINT NULL,
    action          VARCHAR(128) NOT NULL,
    entity_type     VARCHAR(64) NOT NULL,
    entity_id       VARCHAR(64) NULL,
    payload_json    JSON NULL,
    KEY idx_audit_created (created_at),
    KEY idx_audit_correlation (correlation_id),
    KEY idx_audit_user (user_id),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

ALTER TABLE ai_requests_log ADD COLUMN correlation_id VARCHAR(64) NULL AFTER user_id;
CREATE INDEX idx_ai_log_correlation ON ai_requests_log(correlation_id);
