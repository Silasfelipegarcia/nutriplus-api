# Usuários de teste — ambiente local/dev

Com `SPRING_PROFILES_ACTIVE=local,dev`, a API cria automaticamente contas de teste no primeiro boot (idempotente: não recria se o e-mail já existir).

**Senha padrão de todas as contas:** `Nutri123!`

Para recriar do zero: apague os usuários no banco ou use um banco limpo e reinicie a API.

## Contas legadas (compatibilidade)

| E-mail | Cenário |
|--------|---------|
| `teste@nutriplus.local` | Perfil completo (Luna), sem plano — gerar 1º plano |
| `teste2@nutriplus.local` | Sem perfil — fluxo de onboarding |
| `admin@nutriplus.local` | Admin do console |

## Personas (assistentes primários)

| E-mail | Persona | Plano |
|--------|---------|-------|
| `persona.luna@nutriplus.local` | Luna (acolhedora) | Essencial mensal |
| `persona.bruno@nutriplus.local` | Bruno (objetivo) | Essencial mensal |

## Planos de assinatura

| E-mail | Plano | O que validar |
|--------|-------|----------------|
| `plano.essencial@nutriplus.local` | Essencial mensal | Cotas, paywall, features do tier |
| `plano.essencial.anual@nutriplus.local` | Essencial anual | Assinatura anual ativa |
| `plano.atleta@nutriplus.local` | Atleta mensal | Modo atleta + treinos cadastrados |
| `plano.atleta.anual@nutriplus.local` | Atleta anual | Tier atleta anual |
| `plano.teste@nutriplus.local` | TEST_MONTHLY (R$ 1) | Validação de cobrança |
| `trial@nutriplus.local` | Trial 7 dias | Status TRIAL, acesso completo temporário |
| `plano.expirado@nutriplus.local` | Essencial expirado | Status EXPIRED / FREE efetivo |

## Agentes secundários (revisão IA no plano)

Estes agentes não são escolhidos pelo usuário — o perfil dispara a revisão na geração do plano.

| E-mail | Agente | Gatilho no perfil |
|--------|--------|-------------------|
| `helena@nutriplus.local` | Helena (geriatria) | 68 anos |
| `flora@nutriplus.local` | Flora (dieta restrita) | Vegetariana |

Outros agentes internos (Evandro, Mercado, Garcia) são acionados por regras automáticas — ver `nutriplus-agentes/config/agents.yaml`.

## Ciclo de 15 dias e regeneração

| E-mail | Cenário | O que validar no app |
|--------|---------|----------------------|
| `plano.bloqueado@nutriplus.local` | Plano há 3 dias, bloqueio por +12 dias | Correção única disponível; regen bloqueada |
| `correcao.usada@nutriplus.local` | Correção única já usada | Sem correção; aguardar reavaliação |
| `ciclo.vencido@nutriplus.local` | Perfil há 20 dias, lock expirado | Aba **Evolução** — reavaliação disponível |
| `atleta.regen@nutriplus.local` | Atleta com `ATHLETE_SWITCH` elegível | Regeneração ao ativar/mudar modo atleta |

### Fluxo sugerido — ciclo de 15 dias

1. **`teste@nutriplus.local`** — gere o 1º plano (`FIRST_PLAN`).
2. **`plano.bloqueado@nutriplus.local`** — veja banner de bloqueio e teste **correção única**.
3. **`correcao.usada@nutriplus.local`** — confirme que a correção não aparece mais.
4. **`ciclo.vencido@nutriplus.local`** — aba Evolução deve mostrar reavaliação pendente.

## Matriz rápida

| Preciso testar… | Use |
|-----------------|-----|
| Onboarding do zero | `teste2@nutriplus.local` |
| Gerar 1º plano | `teste@nutriplus.local` |
| Plano já existente (stub) | `plano.bloqueado@nutriplus.local` |
| Assinatura ativa | `plano.essencial@` ou `plano.atleta@` |
| Trial | `trial@nutriplus.local` |
| Assinatura vencida | `plano.expirado@nutriplus.local` |
| Luna vs Bruno | `persona.luna@` / `persona.bruno@` |
| Idoso (Helena) | `helena@nutriplus.local` |
| Vegetariano (Flora) | `flora@nutriplus.local` |
| Admin | `admin@nutriplus.local` |

## Implementação

- Seeder: `src/main/java/br/com/nutriplus/infrastructure/dev/DevFunctionalTestSeeder.java`
- Plano stub (sem IA): `DevStubMealPlanFactory.java`
- Profile Spring: `local,dev` (padrão em `application.properties`)

## Resetar usuários de teste

```sql
-- Cuidado: só em banco local de desenvolvimento
DELETE FROM meal_items WHERE meal_id IN (SELECT id FROM meals WHERE meal_plan_id IN (SELECT id FROM meal_plans WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@nutriplus.local')));
DELETE FROM meals WHERE meal_plan_id IN (SELECT id FROM meal_plans WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@nutriplus.local'));
DELETE FROM meal_plans WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@nutriplus.local');
DELETE FROM user_training_activities WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@nutriplus.local');
DELETE FROM nutrition_profiles WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@nutriplus.local');
DELETE FROM users WHERE email LIKE '%@nutriplus.local';
```

Reinicie a API para recriar todas as contas.

## Testes automatizados

Ver [FUNCTIONAL_TESTING.md](./FUNCTIONAL_TESTING.md) — 18 cenários validados em CI via `FunctionalTestUserScenarioIntegrationTest`.
