ALTER TABLE users
    ADD COLUMN access_rejected_at DATETIME NULL,
    ADD COLUMN access_rejected_by BIGINT NULL,
    ADD COLUMN access_rejection_reason VARCHAR(500) NULL;
