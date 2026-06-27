# Nutri+ — Modo Atleta (treinos e calorias)

Ativação **fora do onboarding**, no Perfil — para quem pratica esporte e quer o plano alimentar alinhado ao gasto real de treino.

## Benchmark

| App | O que faz bem | No Nutri+ |
|-----|---------------|-----------|
| **MyFitnessPal** | Exercícios com kcal por duração | Catálogo + MET × peso × tempo |
| **Strava** | Foco em corrida/ciclismo | Esportes no catálogo, não GPS na v1 |
| **Yazio** | Treino soma ao orçamento calórico | `training_daily_extra_kcal` no TDEE |
| **Lifesum** | Modo ativo vs sedentário | `athlete_mode_enabled` separado do nível base |
| **Nike Training** | Planos por tipo de treino | Lista de modalidades (musculação, crossfit…) |

## Fluxo

1. Onboarding: escolha **tipo de perfil** → se atleta, cadastre treinos **antes** de métricas/dieta → salve perfil.
2. Use `POST /onboarding/complete` (atleta) ou `POST /nutrition-profile` + `PUT /training/profile`.
3. Em **Perfil → Modo atleta**: editar treinos em `PUT /training/profile` **já recalcula** metas com o extra de treino.
4. App mostra **kcal por sessão** e **média diária extra**.
5. Gere um **novo plano alimentar** para refletir as metas na aba Plano (`POST /meal-plans/generate`).

`POST /training/apply` permanece como alias retrocompatível (equivalente a salvar treinos com modo atleta ativo).

Ver [ONBOARDING.md](./ONBOARDING.md) para sequência completa.

## Fórmula (Compendium MET)

`kcal_sessão = MET × peso_kg × (minutos / 60)`  
`extra_diário = Σ(dias_semana × kcal_sessão) / 7`

## Endpoints

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/training/sports` | Catálogo de esportes + MET (**público**, sem JWT) |
| GET | `/training/profile` | Modo atleta + treinos + preview kcal |
| PUT | `/training/profile` | Salva treinos e recalcula macros (extra de treino incluído) |
| POST | `/training/apply` | Alias: recalcula metas nutricionais com treino (legado) |
| POST | `/onboarding/complete` | Onboarding atleta: perfil + treino + apply |
