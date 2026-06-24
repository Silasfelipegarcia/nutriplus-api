ALTER TABLE users
    ADD COLUMN cpf_hash VARCHAR(64) NULL,
    ADD COLUMN cpf_encrypted VARCHAR(512) NULL;

CREATE UNIQUE INDEX uk_users_cpf_hash ON users (cpf_hash);
