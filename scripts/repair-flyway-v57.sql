-- Reparo Flyway V57 após falha de deploy (BIGSERIAL / sintaxe PostgreSQL).
-- Rodar no MySQL de prod se flyway_schema_history tiver V57 com success = 0.

-- 1) Estado atual
SELECT version, success, installed_on
FROM flyway_schema_history
WHERE version = '57'
ORDER BY installed_rank DESC;

-- 2) Remover entrada falha (permite Flyway rodar V57 corrigida no próximo deploy)
DELETE FROM flyway_schema_history
WHERE version = '57' AND success = 0;

-- 3) Conferir
SELECT version, success FROM flyway_schema_history WHERE version = '57';
