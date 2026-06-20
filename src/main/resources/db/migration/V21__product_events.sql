-- Product analytics events for funnel and drop-off tracking.

CREATE TABLE product_events (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    session_id      VARCHAR(64) NOT NULL,
    user_id         BIGINT NULL,
    event_name      VARCHAR(128) NOT NULL,
    step            VARCHAR(128) NULL,
    properties_json JSON NULL,
    correlation_id  VARCHAR(64) NULL,
    CONSTRAINT fk_product_events_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_product_events_session (session_id, created_at),
    INDEX idx_product_events_name (event_name, created_at),
    INDEX idx_product_events_user (user_id, created_at),
    INDEX idx_product_events_step (event_name, step, created_at)
);
