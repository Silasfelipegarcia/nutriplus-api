ALTER TABLE nutrition_profiles
    ADD COLUMN birth_date DATE NULL,
    ADD COLUMN state_code VARCHAR(2) NULL,
    ADD COLUMN city VARCHAR(120) NULL,
    ADD COLUMN chewing_difficulty ENUM('NONE', 'MILD', 'SIGNIFICANT') NOT NULL DEFAULT 'NONE',
    ADD COLUMN senior_weight_loss_ack BOOLEAN NOT NULL DEFAULT FALSE;
