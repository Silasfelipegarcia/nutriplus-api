# Onboarding — sequência de endpoints

## Fluxo recomendado (web e mobile)

1. Assistente → tipo de perfil → **treino (se atleta)** → preferências → métricas → dieta → saúde/rotina
2. Persistência final:
   - **Modo geral:** `POST /nutrition-profile`
   - **Modo atleta:** `POST /onboarding/complete` (orquestra perfil + treino + `apply` em uma transação)

## Por que treino antes do primeiro cálculo de macros?

`POST /nutrition-profile` calcula macros via IA **sem** `trainingDailyExtraKcal`.  
`POST /training/apply` recalcula incluindo o gasto dos treinos.

Para atletas, use `POST /onboarding/complete` para evitar estados intermediários inconsistentes.

## Endpoints

| Método | Path | Quando usar |
|--------|------|-------------|
| POST | `/nutrition-profile` | Perfil geral ou atualização pós-onboarding |
| PUT | `/training/profile` | Salvar treinos (exige perfil existente) |
| POST | `/training/apply` | Recalcular macros com treino |
| POST | `/onboarding/complete` | **Onboarding atleta** — perfil + treino + apply |

## Corpo de `/onboarding/complete`

```json
{
  "nutritionProfile": { "...": "mesmos campos de NutritionProfileRequest" },
  "athleteModeEnabled": true,
  "activities": [
    { "sportType": "RUNNING", "daysPerWeek": 3, "minutesPerSession": 45 }
  ]
}
```

## Edição pós-onboarding

- Treino: `GET/PUT /training/profile` + `POST /training/apply`
- Perfil: `POST /nutrition-profile` (recalcula sem treino; atleta deve aplicar treino depois se necessário)

Ver também [TRAINING_MODE.md](./TRAINING_MODE.md).
