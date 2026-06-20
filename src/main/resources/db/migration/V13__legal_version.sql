ALTER TABLE users
    ADD COLUMN terms_version VARCHAR(20) NULL AFTER terms_accepted_at,
    ADD COLUMN privacy_policy_accepted_at TIMESTAMP NULL AFTER terms_version;
