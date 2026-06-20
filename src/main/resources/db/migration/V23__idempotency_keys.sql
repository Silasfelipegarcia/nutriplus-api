CREATE TABLE idempotency_keys (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key         VARCHAR(128) NOT NULL,
    scope_user_id           BIGINT NOT NULL,
    http_method             VARCHAR(10) NOT NULL,
    request_path            VARCHAR(255) NOT NULL,
    request_hash            CHAR(64) NOT NULL,
    status                  ENUM('IN_PROGRESS', 'COMPLETED', 'FAILED') NOT NULL,
    response_status         INT NULL,
    response_body           MEDIUMTEXT NULL,
    response_content_type   VARCHAR(128) NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at              TIMESTAMP NOT NULL,
    CONSTRAINT uq_idempotency_scope
        UNIQUE (idempotency_key, scope_user_id, http_method, request_path)
);

CREATE INDEX idx_idempotency_expires ON idempotency_keys(expires_at);
