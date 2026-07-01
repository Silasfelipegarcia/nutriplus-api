INSERT INTO app_feature_flags (code, name, description, category, enabled)
VALUES (
    'UNLIMITED_PLAN_REGEN',
    'Regeneração livre de plano',
    'Quando ligado, usuários podem gerar novo plano a qualquer momento (ignora trava de 15 dias e correção única). Quando desligado, valem as regras normais de regeração.',
    'PRODUTO',
    0
);
