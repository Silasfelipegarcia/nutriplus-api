ALTER TABLE app_feature_flags
    ADD COLUMN category VARCHAR(32) NOT NULL DEFAULT 'PLATAFORMA' AFTER description;

UPDATE app_feature_flags SET category = 'ACESSO' WHERE code = 'REGISTRATION_OPEN';
UPDATE app_feature_flags SET category = 'PRODUTO' WHERE code IN ('AI_MEAL_PLAN', 'ATHLETE_MODE');
UPDATE app_feature_flags SET category = 'MONETIZACAO' WHERE code = 'SUBSCRIPTION_BILLING';
UPDATE app_feature_flags SET category = 'MARKETPLACE' WHERE code = 'MARKETPLACE_NUTRITIONISTS';
UPDATE app_feature_flags SET category = 'MARKETING' WHERE code = 'APP_STORE_LINKS';

INSERT INTO app_feature_flags (code, name, description, category, enabled)
VALUES (
    'SHOPPING_FINANCE',
    'Aba Economia (usuário)',
    'Exibe a aba Economia no app e no portal — projeção de gasto com alimentação e trocas no mercado',
    'PRODUTO',
    0
);
