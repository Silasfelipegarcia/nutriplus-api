# Metabolismo, composição corporal e modos de cálculo

Documento de **produto** e **contrato técnico** para BMR/TDEE, os três modos de cálculo no onboarding e a diferença entre **meta alimentar** e **evolução corporal**.

---

## Conceitos (negócio)

### Duas coisas diferentes

| Conceito | O que é | Onde o usuário vê |
|----------|---------|-------------------|
| **Modo de cálculo** (`calculationMethod`) | Como o sistema calcula o **metabolismo basal (BMR)** para definir **meta calórica do plano** | Onboarding → Medidas → “Como calcular seu metabolismo?” |
| **Histórico de composição** | Peso, % gordura, circunferências ao longo do tempo | Aba **Evolução**, reavaliação a cada ~15 dias, gráficos |

O usuário **não escolhe “ou meta ou evolução”**. O modo de cálculo serve só para a **dieta de hoje**. A **evolução** usa registros em `body_measurement_sessions` (e opcionalmente o último % gordura espelhado no perfil).

### Os três modos de cálculo

| Modo | Campo no app | O que o usuário informa | Como o BMR é obtido |
|------|----------------|-------------------------|---------------------|
| `ESTIMATE` | Estimativa (idade, peso e altura) | Altura, peso, idade (já no fluxo) | Fórmula **Mifflin-St Jeor** |
| `BIOIMPEDANCE` | Bioimpedância (% de gordura) | **% gordura** do laudo | **Katch-McArdle** a partir da massa magra estimada |
| `MANUAL_BMR` | Valor da balança (TMB em kcal) | **TMB / metabolismo basal** em kcal/dia do laudo | Valor **informado pelo usuário** (sem recalcular) |

Se o usuário **não** tiver laudo de bio, o padrão é `ESTIMATE`.

Depois do BMR:

1. **TDEE** = BMR × fator de atividade (sedentário → intenso)
2. **Gasto total** = TDEE + extra de treino (modo atleta)
3. **Meta calórica** = gasto total ± ajuste do objetivo (emagrecer / manter / ganhar massa), com piso de segurança e **sem ultrapassar o gasto** em emagrecimento

### % de gordura e evolução

| Onde fica | Tabela / campo | Uso |
|-----------|----------------|-----|
| Snapshot no perfil | `nutrition_profiles.body_fat_percent` | Último % conhecido; usado no modo `BIOIMPEDANCE` para BMR; atualizado quando o usuário salva medição com % |
| Histórico | `body_measurement_sessions.body_fat_percent` | **Gráfico de evolução**, análise de progresso IA, comparação entre datas |
| TMB manual | `nutrition_profiles.manual_bmr_kcal` | Só quando `calculationMethod = MANUAL_BMR` |

**Regra de produto:** o usuário pode informar **% gordura opcional** nas medidas do onboarding mesmo em `ESTIMATE` ou `MANUAL_BMR`. Isso alimenta o perfil e a primeira linha de evolução; o **modo de cálculo** continua definindo só qual número entra no BMR.

Fluxos que gravam % gordura no histórico:

- `POST /progress/measurements` (reavaliação ~15 dias)
- Fluxo de gerar plano com medidas (`GenerateMealPlanFlowScreen` / portal progresso)
- Onboarding / edição de medidas (campo opcional)

Ao salvar medição com `bodyFatPercent`, a API **atualiza o perfil** (`ProgressService`) para manter peso e % alinhados.

---

## Pipeline técnico (meta calórica)

```
Perfil (peso, altura, idade, modo, bodyFat/manualBmr, objetivo, treino)
        │
        ▼
POST /api/v1/nutrition/calculate  (nutriplus-agentes)
        │
        ▼
BMR → TDEE → meta + macros (targetCalories, protein, carbs, fat)
        │
        ▼
Salvo em nutrition_profiles.*
        │
        ▼
POST /meal-plans/generate
  • API recalcula macros antes do job (refreshMacroTargets)
  • Agentes sincroniza meta (sync_meal_plan_targets)
  • Plano alinhado à meta (±12%), rebalance após scrub/distribuição
```

### Guardrails de emagrecimento (agentes)

- Meta não pode ficar **acima do gasto total** por causa de piso calórico fixo (1200/1500)
- Déficit mínimo desejável: ~250 kcal abaixo do gasto total quando possível
- `align_plan_to_target` após distribuição por refeição no `_finalize_plan`

---

## API

### Enum `CalculationMethod`

`ESTIMATE` | `BIOIMPEDANCE` | `MANUAL_BMR`

Migration: `V55__manual_bmr.sql` — coluna `manual_bmr_kcal`.

### `NutritionProfileRequest`

| Campo | Obrigatório quando |
|-------|-------------------|
| `calculationMethod` | Opcional (default `ESTIMATE`) |
| `bodyFatPercent` | Obrigatório se `BIOIMPEDANCE`; **opcional** nos outros modos |
| `manualBmrKcal` | Obrigatório se `MANUAL_BMR` (800–5000 kcal) |

### Endpoints relacionados

| Método | Path | Papel |
|--------|------|--------|
| POST | `/nutrition-profile` | Salva perfil + recalcula macros via agente |
| POST | `/meal-plans/generate` | Recalcula macros no perfil → gera plano |
| POST | `/progress/measurements` | Histórico + atualiza peso/% no perfil |
| GET | `/progress/measurements/latest` | Pré-preenche formulários |
| POST | `/api/v1/nutrition/calculate` | Motor determinístico (agentes) |

---

## UI (Flutter)

| Tela | Comportamento |
|------|----------------|
| `MetricsBodyScreen` | Dropdown dos 3 modos + campo específico + **% gordura opcional** |
| `MetabolismSummaryCard` | Meta, TDEE, BMR, gasto total, déficit/superávit |
| `EvolutionTabScreen` / `ProgressReviewScreen` | Histórico e gráfico de % gordura |
| `GenerateMealPlanFlowScreen` | Medidas com % gordura opcional antes de gerar plano |

---

## Pro (nutricionista)

`POST /pro/patients/{id}/measurements` aceita `calculationMethod` + medidas. Ver [NUTRI_PLUS_PRO.md](./NUTRI_PLUS_PRO.md) §7.

---

## Referências no código

| Repo | Arquivos principais |
|------|---------------------|
| nutriplus-agentes | `app/nutrition_engine.py`, `app/meal_plan_generator.py` (`sync_meal_plan_targets`) |
| nutriplus-api | `NutritionProfileService`, `MealPlanGenerationProcessor`, `ProgressService` |
| nutriplus-frontend | `metrics_body_screen.dart`, `metabolism_summary_card.dart`, `evolution_trend_chart.dart` |

Ver também: [PROGRESS_ANALYSIS.md](./PROGRESS_ANALYSIS.md), [ONBOARDING.md](./ONBOARDING.md), [RELEASE_NOTES_2025-07.md](./RELEASE_NOTES_2025-07.md).
