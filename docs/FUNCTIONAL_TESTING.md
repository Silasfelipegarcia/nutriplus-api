# Testes funcionais automatizados — Nutri+

Três camadas de validação automática cobrindo planos, personas, ciclo de 15 dias e fluxos principais.

## CI / CD — gate obrigatório

Cada repositório tem um job **`Functional gate`** no GitHub Actions. O PR/push **só deve subir** se esse check estiver verde.

| Repositório | Workflow | Job | O que valida |
|-------------|----------|-----|--------------|
| `nutriplus-api` | `.github/workflows/ci.yml` | **Functional gate (API)** | 171 unit + 35 integração (18 cenários + fluxos) |
| `nutriplus-frontend` | `.github/workflows/ci.yml` | **Functional gate (Flutter)** | analyze + 104 testes |
| `nutriplus-agentes` | `.github/workflows/ci.yml` | **Functional gate (Agentes)** | 159 pytest |

### API — script usado no CI

```bash
cd nutriplus-api
./scripts/ci-functional-gate.sh
```

Equivalente local à suíte completa do monorepo:

```bash
./run-functional-tests.sh   # raiz do workspace (API + agentes + Flutter)
```

### Branch protection (recomendado)

No GitHub → **Settings → Branches → Branch protection** para `main` / `homolog`:

1. **Require status checks** antes do merge
2. Marcar: `Functional gate (API)` (e Flutter/Agentes nos respectivos repos)
3. **Require branches to be up to date**

Assim nada entra em produção sem passar pelos cenários funcionais.

## 1. Testes de integração (automático no CI)

| Classe | O que valida |
|--------|----------------|
| `FunctionalTestUserScenarioIntegrationTest` | **18 cenários** do `DevTestUserSpec` (parametrizado) |
| `FunctionalFlowIntegrationTest` | Onboarding, admin, atleta, Helena, Flora |

```bash
cd nutriplus-api
mvn test -Dtest='FunctionalTestUserScenarioIntegrationTest,FunctionalFlowIntegrationTest'
```

### Cenários cobertos automaticamente

- Personas Luna e Bruno
- Planos: FREE, Essencial M/A, Atleta M/A, TEST_MONTHLY, Trial, Expirado
- Agentes: Helena (68a), Flora (vegetariana)
- Ciclo 15d: bloqueado, correção usada, reavaliação vencida, atleta regen
- Admin e onboarding sem perfil

## 2. Suíte completa local

```bash
cd nutriplus-api
chmod +x scripts/run-functional-tests.sh
./scripts/run-functional-tests.sh
```

Executa:
1. `./scripts/ci-functional-gate.sh` — unit + integração + JaCoCo
2. `pytest` no nutriplus-agentes (se existir)
3. `flutter analyze` + `flutter test` (se existir)

Variáveis opcionais:

```bash
RUN_FLUTTER=0 RUN_AGENTES=0 ./scripts/run-functional-tests.sh
RUN_LOCAL_API_SMOKE=1 API_BASE_URL=http://localhost:8080 ./scripts/run-functional-tests.sh
```

## 3. Smoke contra API local com usuários dev

Com API rodando (`local,dev`) e usuários seedados:

```bash
python3 nutriplus-api/scripts/functional_dev_users_validate.py
```

Valida login + perfil + assinatura + plano + elegibilidade de regeneração para **todos** os `@nutriplus.local`.

## Arquivos principais

| Arquivo | Função |
|---------|--------|
| `infrastructure/dev/DevTestUserSpec.java` | Catálogo de cenários |
| `infrastructure/dev/FunctionalTestUserSeeder.java` | Seeder compartilhado (dev + testes) |
| `test/.../FunctionalTestUserScenarioIntegrationTest.java` | Teste parametrizado |
| `test/.../FunctionalFlowIntegrationTest.java` | Fluxos E2E HTTP |
| `test/.../FunctionalTestUserAssertions.java` | Asserções reutilizáveis |
| `scripts/ci-functional-gate.sh` | Gate da API (CI + pré-push) |
| `scripts/run-functional-tests.sh` | Runner da suíte completa |
| `scripts/functional_dev_users_validate.py` | Smoke HTTP local |
| `docs/DEV_TEST_USERS.md` | Catálogo manual de contas |

## Próximo passo (opcional)

- `integration_test/` Flutter com Patrol para UI real em device/emulador
- k6 nightly com matriz de usuários dev em homolog
