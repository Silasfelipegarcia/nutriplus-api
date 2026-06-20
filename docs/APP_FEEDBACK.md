# Avaliação in-app (Likert + sugestões)

Tabela: `user_app_feedback` (migration `V22__user_app_feedback.sql`).

Endpoints:

| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/feedback/app` | Envia avaliação (JWT) |
| GET | `/feedback/app/latest` | Última avaliação do usuário autenticado |

## Queries úteis para insights

### Médias por dimensão (últimos 30 dias)

```sql
SELECT
  AVG(ease_of_use) AS ease,
  AVG(meal_plan_quality) AS plan,
  AVG(ai_helpfulness) AS ai,
  AVG(progress_tracking) AS progress,
  AVG(overall_satisfaction) AS overall,
  COUNT(*) AS n
FROM user_app_feedback
WHERE created_at >= NOW() - INTERVAL 30 DAY;
```

### Sugestões recentes com nota geral baixa (≤ 3)

```sql
SELECT user_id, overall_satisfaction, improvement_suggestions, platform, app_version, created_at
FROM user_app_feedback
WHERE overall_satisfaction <= 3
  AND improvement_suggestions IS NOT NULL
ORDER BY created_at DESC
LIMIT 50;
```

### Distribuição da satisfação geral

```sql
SELECT overall_satisfaction, COUNT(*) AS n
FROM user_app_feedback
GROUP BY overall_satisfaction
ORDER BY overall_satisfaction;
```

### Médias por plataforma e versão do app

```sql
SELECT
  platform,
  app_version,
  AVG(overall_satisfaction) AS avg_overall,
  COUNT(*) AS n
FROM user_app_feedback
GROUP BY platform, app_version
ORDER BY n DESC;
```

### Volume semanal

```sql
SELECT
  DATE_FORMAT(created_at, '%Y-%u') AS week_key,
  COUNT(*) AS submissions
FROM user_app_feedback
GROUP BY week_key
ORDER BY week_key DESC
LIMIT 12;
```

## Pontos de atenção na análise

- Cruzar `overall_satisfaction` com `platform` e `app_version` para detectar regressões por release.
- Priorizar textos de usuários com nota ≤ 3 — contêm sinais de churn.
- Comparar volume in-app com reviews públicos das lojas (consulta manual).
- Múltiplas submissões do mesmo `user_id` são permitidas — use `created_at` para tendência temporal.
