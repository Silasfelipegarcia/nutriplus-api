ALTER TABLE users
    ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN password_must_change TINYINT(1) NOT NULL DEFAULT 0;
