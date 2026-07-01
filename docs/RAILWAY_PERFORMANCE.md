# Railway — performance e cold start

## Sintoma

Endpoints públicos em prod (`/health`, `/feature-flags`, `/legal/*`) com p95 ~500–600 ms no audit **sem cold start**, e picos de **5–25 s TTFB** na primeira request após idle (scale-to-zero ou instância fria).

O código Java é rápido localmente (Tier S p95 agregado ~54 ms). O gargalo em prod é **infra + rede**, não query lenta.

## Diagnóstico

```bash
# Warm-up + audit (5 amostras, cold vs warm)
export PERF_TEST_EMAIL='...'
export PERF_TEST_PASSWORD='...'
cd nutriplus-api
python3 perf/audit-endpoints.py https://nutriplus-api-production.up.railway.app \
  --env prod --samples 5 --skip-mutations \
  --out perf/results/audit-prod-$(date +%Y%m%d-%H%M%S).json
```

Compare colunas **cold** vs **warm** no relatório. Se `cold_ms >> warm_avg_ms` em todos os endpoints, é cold start.

## Ações recomendadas (Railway)

| Ação | Impacto | Como |
|------|---------|------|
| **Min 1 réplica** | Elimina cold start na maioria dos acessos | Railway → Service → Settings → desabilitar scale-to-zero / `numReplicas: 1` |
| **Health ping externo** | Mantém instância quente entre acessos | Cron (GitHub Actions / UptimeRobot) `GET /health` a cada 5 min |
| **Região** | Latência Brasil | Preferir `us-east` ou região mais próxima dos usuários |
| **Cache HTTP público** | Reduz hits repetidos | Já em `PublicHttpCacheFilter` (`/feature-flags`, `/plans`, `/legal/*`) |
| **Cache app** | Reduz DB em instância quente | `nutriplus.cache.enabled=true` + Redis no Railway (TTLs em `application-prod.properties`) |
| **Índices DB** | Queries de checkins / security_events | Migration `V57__performance_indexes.sql` |
| **JVM** | Menos pausa de GC sob carga | `docker-entrypoint.sh`: `-Xmx2g`, G1GC, `MaxGCPauseMillis=200` |

## Meta pós-ajuste

| Métrica | Antes (cold) | Meta warm |
|---------|--------------|-----------|
| `/health` TTFB | 5–15 s | < 300 ms |
| `/feature-flags` p95 | 500–25000 ms | < 400 ms |
| Dashboard flow (auth) | não medido | < 800 ms soma p95 |

## Monitoramento contínuo

- CI nightly: `.github/workflows/k6-nightly.yml` (audit autenticado com secrets)
- Admin: `/admin/performance/summary` no portal
- Stress manual: `PERF_TEST_EMAIL=... PERF_TEST_PASSWORD=... ./perf/run-stress-prod.sh`
