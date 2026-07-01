# Release notes — julho 2025

Novidades de produto e referência técnica consolidada (API + agentes + app Flutter). Deploy: branch `main` → Railway (API e agentes); APK de teste gerado a partir de `nutriplus-frontend` com `config/prod.json`.

---

## Resumo executivo (negócio)

1. **Login com CPF** — usuários com CPF criptografado incompatível não são mais bloqueados no login.
2. **Meta calórica em emagrecimento** — plano e meta não ficam acima do gasto estimado por causa de piso calórico ou pipeline desalinhado.
3. **TMB da bioimpedância** — usuário pode informar o valor de metabolismo basal da balança em kcal/dia.
4. **Distribuição de refeições** — padrão equilibrado ou “mais fome no final do dia”.
5. **Modo low carb** — moderado ou rigoroso, com saldo de carboidratos no dia e em comida fora do plano.
6. **Comida fora do plano em gramas** — estimativa correta (ex.: 10 g macarrão ≈ 13 kcal, não milhares).
7. **Dislikes genéricos** — ex.: “frutas” bloqueia frutas no plano de forma consistente.

---

## 1. Autenticação — CPF (API)

**Problema:** após login válido, a API falhava ao montar `UserResponse` com CPF mascarado (`Falha ao descriptografar CPF`).

**Solução:** falha de descriptografia para **exibição** retorna `null` em vez de erro HTTP 400.

| Repo | Commit / nota |
|------|----------------|
| nutriplus-api | `c22fa82` — `CpfProtectionService.maskFromEncrypted` tolerante |

---

## 2. Meta calórica e plano alimentar (agentes + API)

**Problema:** em emagrecimento, alguns usuários recebiam plano/meta **acima do gasto** (ganho de peso).

**Causas corrigidas:**

- Piso 1200/1500 kcal podia **ultrapassar o TDEE** em perfis com gasto baixo
- Meta do perfil **não era recalculada** imediatamente antes de gerar o plano
- Distribuição por refeição adicionava calorias **depois** do alinhamento final

**Solução:**

- Teto de meta em `LOSE_WEIGHT`: abaixo do gasto total (déficit mínimo ~250 kcal quando possível)
- `sync_meal_plan_targets` no agente antes da geração
- `refreshMacroTargets` na API no `MealPlanGenerationProcessor`
- `align_plan_to_target` após `apply_meal_distribution` no `_finalize_plan`

| Repo | Commit |
|------|--------|
| nutriplus-agentes | `879b554` |
| nutriplus-api | `6c3057c` (parcial — recálculo no generate) |

Doc: [METABOLISM_AND_BODY_COMPOSITION.md](./METABOLISM_AND_BODY_COMPOSITION.md)

---

## 3. Três modos de metabolismo + TMB manual

**Produto:** na etapa **Medidas**, o usuário escolhe como calcular o BMR:

- Estimativa (padrão)
- Bioimpedância (% gordura)
- Valor da balança (TMB em kcal)

**% gordura para evolução:** independente do modo; histórico em medições periódicas + campo opcional no onboarding. Ver [METABOLISM_AND_BODY_COMPOSITION.md](./METABOLISM_AND_BODY_COMPOSITION.md).

| Repo | Alteração |
|------|-----------|
| nutriplus-api | `V55__manual_bmr.sql`, enum `MANUAL_BMR`, `manual_bmr_kcal` |
| nutriplus-agentes | `nutrition_engine` usa TMB informada |
| nutriplus-frontend | `MetricsBodyScreen` dropdown 3 modos |

Commits: API `6c3057c`, agentes `879b554`, frontend `a1a3249`.

---

## 4. Distribuição calórica e fome noturna

**Produto:** toggle “Sinto mais fome no final do dia” (`hungerPattern`: `BALANCED` | `MORE_EVENING`). Cotas maiores no jantar e lanche da tarde.

| Camada | Detalhe |
|--------|---------|
| DB | `V54` — `nutrition_profiles.hunger_pattern` |
| Agentes | `meal_distribution.py` — `enforce_meal_distribution`, complementos de volume |
| App | `meal_routine_picker.dart` |

Commit agentes: `9f59acd` (release anterior na mesma linha de features).

---

## 5. Modo low carb

**Produto:** `STANDARD` | `LOW_CARB_MODERATE` | `LOW_CARB_STRICT` no perfil; meta de carboidratos no plano; saldo diário e impacto em comida fora do plano.

| Camada | Detalhe |
|--------|---------|
| DB | `V54` — `nutrition_mode`, `daily_food_extras.estimated_carbs_g` |
| Agentes | `nutrition_engine`, `guardrails.validate_carb_cap`, `food_extra_estimator` |
| API | `CheckinService` — `consumedCarbsG`, `remainingCarbsG` |
| App | `dietary_screen.dart`, `today_balance_sheet.dart` |

---

## 6. Comida fora do plano — gramas e carboidratos

**Problema:** “10 gramas de macarrão” gerava ~3000 kcal.

**Solução:** `parse_food_quantity` + tabela kcal/100 g antes do LLM; `estimated_carbs_g` em low carb.

Commit agentes: `d967e3d`.

---

## 7. Deploy e migrations

| Serviço | Migrations novas | Variáveis |
|---------|------------------|-----------|
| nutriplus-api | V54, V55 | `CPF_ENCRYPTION_KEY`, Flyway no boot |
| nutriplus-agentes | — | `AI_AGENT_URL` na API |

**App de teste:** `flutter build apk --release --dart-define-from-file=config/prod.json` → `nutriplus-v1.1.0-prod.apk`

Após deploy: usuários devem **gerar novo plano** para aplicar metas e distribuição atualizadas.

---

## Checklist pós-deploy (QA)

- [ ] Login com conta que falhava por CPF
- [ ] Perfil emagrecimento: meta &lt; gasto total no card de metabolismo
- [ ] Modo TMB manual: informar kcal da balança → meta coerente
- [ ] Gerar plano: `totalCalories` dentro de ~12% da meta
- [ ] Low carb: saldo de carbs no Hoje ao registrar extra
- [ ] Extra “10 g arroz”: kcal plausível (&lt; 50)

---

## Índice de documentação atualizada

| Documento | Conteúdo |
|-----------|----------|
| [METABOLISM_AND_BODY_COMPOSITION.md](./METABOLISM_AND_BODY_COMPOSITION.md) | Modos de cálculo vs evolução |
| [ONBOARDING.md](./ONBOARDING.md) | Fluxo + modos de metabolismo |
| [FEATURES.md](./FEATURES.md) | Catálogo atualizado |
| [PROGRESS_ANALYSIS.md](./PROGRESS_ANALYSIS.md) | % gordura no histórico |
| [NUTRI_PLUS_PRO.md](./NUTRI_PLUS_PRO.md) | Bio + MANUAL_BMR no Pro |
