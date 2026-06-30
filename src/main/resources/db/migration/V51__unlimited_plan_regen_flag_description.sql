UPDATE app_feature_flags
SET description = 'Modo teste: ignora trava de 15 dias, correção única, cota diária (2/dia no beta) e cota mensal. Usuários podem gerar quantos planos quiserem. Desligado = regras normais de produto.'
WHERE code = 'UNLIMITED_PLAN_REGEN';
