-- Plano de teste R$ 1,00 para validar cobrança real em produção (desative no admin após validar).

INSERT INTO subscription_plan_catalog
    (plan_code, name, description, price_cents, period_days, price_suffix, benefits_json,
     trial_available, contact_sales, enabled, visible_in_catalog, sort_order)
VALUES
    ('TEST_MONTHLY', 'Teste Produção (R$ 1)',
     'Somente para validar pagamento real — mesmo acesso do Essencial por 30 dias',
     100, 30, '/mês',
     JSON_ARRAY(
         'Cobrança de R$ 1,00 no cartão',
         'Acesso Essencial por 30 dias',
         'Desative este plano no admin após o teste'
     ),
     0, 0, 1, 1, 0);
