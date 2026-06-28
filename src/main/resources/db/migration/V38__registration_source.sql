ALTER TABLE users
    ADD COLUMN registration_source VARCHAR(32) NOT NULL DEFAULT 'OPEN' AFTER login_enabled_by;
