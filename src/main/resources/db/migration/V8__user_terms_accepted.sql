ALTER TABLE users
    ADD COLUMN terms_accepted_at TIMESTAMP NULL AFTER password_must_change;
