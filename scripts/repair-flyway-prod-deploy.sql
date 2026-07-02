-- Reparo Flyway prod (Railway) — deploy bloqueado com jpaSharedEM_entityManagerFactory.
-- Causa real: migration V56 ou V57 com success = 0 em flyway_schema_history.
--
-- Rodar no MySQL do Railway (serviço MySQL → Connect → Query).
-- Schema costuma ser `railway`.

USE railway;

-- 1) Diagnóstico
SELECT installed_rank, version, description, script, success, installed_on
FROM flyway_schema_history
WHERE version IN ('56', '57')
ORDER BY installed_rank;

SELECT version, success, COUNT(*) AS rows
FROM flyway_schema_history
WHERE version IN ('56', '57')
GROUP BY version, success;

-- Colunas V57 (podem existir mesmo com V57 falha parcial)
SELECT COLUMN_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'nutritionists'
  AND COLUMN_NAME IN ('formation', 'experience_years', 'approach', 'languages');

SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'nutritionist_portfolio_items';

-- 2) Remover entradas falhas (obrigatório antes do próximo deploy)
DELETE FROM flyway_schema_history
WHERE version IN ('56', '57') AND success = 0;

-- 3) Conferir — não deve restar success = 0
SELECT version, success, installed_on
FROM flyway_schema_history
WHERE version IN ('56', '57')
ORDER BY installed_rank;

-- 4) Redeploy da API (imagem com ec6034f ou posterior).
--    V56/V57 idempotentes aplicam o que faltar sem duplicar índices/colunas.
