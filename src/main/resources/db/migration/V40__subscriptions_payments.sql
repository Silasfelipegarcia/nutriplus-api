ALTER TABLE users
    ADD COLUMN subscription_plan VARCHAR(30) NOT NULL DEFAULT 'FREE',
    ADD COLUMN plan_valid_until TIMESTAMP(6) NULL,
    ADD COLUMN plan_cancelled_at TIMESTAMP(6) NULL,
    ADD COLUMN auto_renew TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN default_card_id VARCHAR(50) NULL,
    ADD COLUMN trial_utilizado TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN trial_ate TIMESTAMP(6) NULL,
    ADD COLUMN mp_customer_id VARCHAR(100) NULL,
    ADD COLUMN athlete_grace_until TIMESTAMP(6) NULL;

CREATE INDEX idx_users_mp_customer ON users (mp_customer_id);
CREATE INDEX idx_users_plan_valid_until ON users (plan_valid_until);

CREATE TABLE payment_orders (
    id              CHAR(36) NOT NULL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    plan            VARCHAR(30) NOT NULL,
    mp_preference_id VARCHAR(100) NULL,
    mp_payment_id   VARCHAR(100) NULL,
    status          VARCHAR(30) NOT NULL,
    amount_cents    INT NOT NULL,
    paid_at         TIMESTAMP(6) NULL,
    renewal         TINYINT(1) NOT NULL DEFAULT 0,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_payment_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_payment_orders_user ON payment_orders (user_id, created_at DESC);
CREATE INDEX idx_payment_orders_mp_payment ON payment_orders (mp_payment_id);
CREATE INDEX idx_payment_orders_mp_preference ON payment_orders (mp_preference_id);

UPDATE users u
INNER JOIN nutrition_profiles np ON np.user_id = u.id
SET u.athlete_grace_until = DATE_ADD(UTC_TIMESTAMP(6), INTERVAL 30 DAY)
WHERE np.athlete_mode_enabled = 1;
