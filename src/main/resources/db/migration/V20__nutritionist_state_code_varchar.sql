-- Hibernate maps String to VARCHAR; V18 used CHAR(2) which fails schema validation.
ALTER TABLE nutritionists
    MODIFY COLUMN state_code VARCHAR(2) NULL;
