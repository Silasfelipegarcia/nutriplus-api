-- V26: security events for risk engine audit trail

CREATE TABLE security_events (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NULL,
    client_ip       VARCHAR(45) NOT NULL,
    action          VARCHAR(64) NOT NULL,
    score           INT NOT NULL,
    blocked         BOOLEAN NOT NULL DEFAULT FALSE,
    details         VARCHAR(2000) NULL,
    correlation_id  VARCHAR(64) NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_security_events_created (created_at),
    INDEX idx_security_events_ip (client_ip),
    INDEX idx_security_events_user (user_id)
);
