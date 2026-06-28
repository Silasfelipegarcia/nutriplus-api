CREATE TABLE subscription_plan_catalog (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_code        VARCHAR(30)  NOT NULL,
    name             VARCHAR(128) NOT NULL,
    description      VARCHAR(512) NULL,
    price_cents      INT          NOT NULL DEFAULT 0,
    period_days      INT          NOT NULL DEFAULT 30,
    price_suffix     VARCHAR(16)  NULL,
    benefits_json    JSON         NULL,
    trial_available  TINYINT(1)   NOT NULL DEFAULT 0,
    contact_sales    TINYINT(1)   NOT NULL DEFAULT 0,
    enabled          TINYINT(1)   NOT NULL DEFAULT 1,
    visible_in_catalog TINYINT(1) NOT NULL DEFAULT 1,
    sort_order       INT          NOT NULL DEFAULT 0,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       BIGINT       NULL,
    UNIQUE KEY uk_subscription_plan_catalog_code (plan_code),
    CONSTRAINT fk_subscription_plan_catalog_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

INSERT INTO subscription_plan_catalog
    (plan_code, name, description, price_cents, period_days, price_suffix, benefits_json, trial_available, contact_sales, enabled, visible_in_catalog, sort_order)
VALUES
    ('FREE', 'Grátis', 'IA, plano alimentar, check-ins e evolução', 0, 0, NULL,
     JSON_ARRAY('Plano alimentar com IA', 'Check-ins e evolução', 'Lista de compras inteligente'),
     0, 0, 1, 1, 0),
    ('ATHLETE_MONTHLY', 'Atleta Mensal', 'Modo atleta, treinos, MET e macros alinhados', 2490, 30, '/mês',
     JSON_ARRAY('Modo atleta completo', 'Treinos + gasto calórico (MET)', 'Recálculo automático de macros', '7 dias grátis com cartão cadastrado'),
     1, 0, 1, 1, 1),
    ('ATHLETE_YEARLY', 'Atleta Anual', 'Tudo do mensal com economia anual', 19900, 365, '/ano',
     JSON_ARRAY('Tudo do plano mensal', 'Economia vs. 12 meses avulsos', 'Renovação automática anual'),
     0, 0, 1, 1, 2);

INSERT INTO app_feature_flags (code, name, description, enabled)
VALUES ('SUBSCRIPTION_BILLING', 'Cobrança de planos', 'Exige assinatura paga para modo atleta e habilita checkout/trial', 0);
