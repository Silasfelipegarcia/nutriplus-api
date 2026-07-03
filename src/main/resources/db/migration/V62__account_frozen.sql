ALTER TABLE users
    ADD COLUMN account_frozen_at DATETIME NULL AFTER access_rejection_reason;

CREATE INDEX idx_users_account_frozen_at ON users (account_frozen_at);
