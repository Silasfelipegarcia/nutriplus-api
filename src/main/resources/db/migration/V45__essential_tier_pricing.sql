-- Opção A: Essencial (R$ 19,90) + Atleta (R$ 29,90) + anuais com desconto

UPDATE subscription_plan_catalog
SET description = '1 geração de plano/mês, check-ins e evolução (com cobrança ativa)',
    benefits_json = JSON_ARRAY(
        '1 plano alimentar IA por mês',
        'Check-ins e evolução',
        'Lista de compras',
        'Chat Luna/Bruno'
    )
WHERE plan_code = 'FREE';

UPDATE subscription_plan_catalog
SET price_cents = 2990,
    name = 'Atleta Mensal',
    description = 'Treinos, MET, macros extras e planos ilimitados',
    benefits_json = JSON_ARRAY(
        'Tudo do Essencial',
        'Modo atleta + treinos (MET)',
        'Regenerações de plano ilimitadas',
        'Macros alinhados ao gasto'
    ),
    trial_available = 0
WHERE plan_code = 'ATHLETE_MONTHLY';

UPDATE subscription_plan_catalog
SET price_cents = 26900,
    name = 'Atleta Anual',
    description = 'Modo atleta com economia anual (~25%)',
    benefits_json = JSON_ARRAY(
        'Tudo do Atleta Mensal',
        'Economia vs. 12 meses avulsos',
        'Renovação automática anual'
    )
WHERE plan_code = 'ATHLETE_YEARLY';

UPDATE subscription_plan_catalog SET sort_order = 3 WHERE plan_code = 'ATHLETE_MONTHLY';
UPDATE subscription_plan_catalog SET sort_order = 4 WHERE plan_code = 'ATHLETE_YEARLY';

INSERT INTO subscription_plan_catalog
    (plan_code, name, description, price_cents, period_days, price_suffix, benefits_json,
     trial_available, contact_sales, enabled, visible_in_catalog, sort_order)
VALUES
    ('ESSENTIAL_MONTHLY', 'Essencial Mensal',
     'Plano IA completo, check-ins e 1 regeneração/mês',
     1990, 30, '/mês',
     JSON_ARRAY(
         'Plano alimentar IA personalizado',
         'Check-ins, evolução e lista de compras',
         '1 regeneração de plano por mês',
         '7 dias grátis com cartão (trial)'
     ),
     1, 0, 1, 1, 1),
    ('ESSENTIAL_YEARLY', 'Essencial Anual',
     'Essencial com economia anual (~25%)',
     17900, 365, '/ano',
     JSON_ARRAY(
         'Tudo do Essencial Mensal',
         'Economia vs. 12 meses avulsos',
         'Renovação automática anual'
     ),
     0, 0, 1, 1, 2);
