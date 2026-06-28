ALTER TABLE users
    ADD COLUMN acquisition_source VARCHAR(64) NULL,
    ADD COLUMN acquisition_medium VARCHAR(64) NULL,
    ADD COLUMN acquisition_campaign VARCHAR(128) NULL,
    ADD COLUMN acquisition_landing VARCHAR(128) NULL;
