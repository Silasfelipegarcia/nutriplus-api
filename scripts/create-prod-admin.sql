-- =============================================================================
-- NutriPlus PROD — criar ou promover administrador
-- Rode no DBeaver (database: railway)
-- =============================================================================

USE railway;

-- ---------------------------------------------------------------------------
-- OPÇÃO A (recomendada): promover conta que você já cadastrou no app/web
-- Troque o e-mail abaixo:
-- ---------------------------------------------------------------------------
/*
UPDATE users
SET role = 'ADMIN',
    login_enabled = 1,
    login_enabled_at = NOW()
WHERE email = 'seu.email@exemplo.com';

SELECT id, name, email, role, login_enabled FROM users WHERE email = 'seu.email@exemplo.com';
*/

-- ---------------------------------------------------------------------------
-- OPÇÃO B: criar admin novo do zero
-- E-mail: admin@nutriplus.app.br
-- Senha inicial: Nutri123!  (TROQUE após o primeiro login)
-- Hash BCrypt compatível com a API (Spring BCryptPasswordEncoder)
-- ---------------------------------------------------------------------------

-- Só insere se o e-mail ainda não existir
INSERT INTO users (
    name,
    email,
    role,
    login_enabled,
    login_enabled_at,
    password_hash,
    registration_source
)
SELECT
    'Admin NutriPlus',
    'admin@nutriplus.app.br',
    'ADMIN',
    1,
    NOW(),
    '$2a$10$qQYLBN3k4FzswyangeRZYe0mgmNpf.dEutAh7kkAHBsxYgczYpfU2',
    'OPEN'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@nutriplus.app.br'
);

-- Se o e-mail já existir, só promove a admin:
UPDATE users
SET role = 'ADMIN',
    login_enabled = 1,
    login_enabled_at = NOW()
WHERE email = 'admin@nutriplus.app.br';

COMMIT;

SELECT id, name, email, role, login_enabled, created_at
FROM users
WHERE email = 'admin@nutriplus.app.br';

-- Login web admin: https://nutriplus.app.br/auth/login
-- Console: https://nutriplus.app.br/admin
