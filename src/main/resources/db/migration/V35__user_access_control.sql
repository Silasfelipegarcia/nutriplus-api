ALTER TABLE users
    ADD COLUMN login_enabled TINYINT(1) NOT NULL DEFAULT 0 AFTER role,
    ADD COLUMN login_enabled_at DATETIME NULL AFTER login_enabled,
    ADD COLUMN login_enabled_by BIGINT NULL AFTER login_enabled_at;

ALTER TABLE users
    ADD CONSTRAINT fk_users_login_enabled_by FOREIGN KEY (login_enabled_by) REFERENCES users (id);

-- Usuários já existentes mantêm acesso; novos cadastros ficam com login_enabled = 0 (default).
UPDATE users
SET login_enabled = 1,
    login_enabled_at = COALESCE(created_at, NOW())
WHERE login_enabled = 0;

CREATE TABLE app_feature_flags (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    enabled     TINYINT(1)   NOT NULL DEFAULT 0,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by  BIGINT       NULL,
    UNIQUE KEY uk_feature_flags_code (code),
    CONSTRAINT fk_feature_flags_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

INSERT INTO app_feature_flags (code, name, description, enabled)
VALUES ('REGISTRATION_OPEN', 'Cadastro aberto', 'Permite novos cadastros no app e na web', 1),
       ('ATHLETE_MODE', 'Modo atleta', 'Exibe treinos e modo atleta no app', 1),
       ('MARKETPLACE_NUTRITIONISTS', 'Marketplace nutricionistas', 'Lista nutricionistas para contratação', 1),
       ('AI_MEAL_PLAN', 'Plano alimentar IA', 'Geração de plano alimentar via agentes', 1);
