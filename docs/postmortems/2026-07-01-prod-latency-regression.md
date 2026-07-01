# Postmortem: API rápida no local, lixo em prod

**Data do incidente:** 2026-06-28 a 2026-07-01  
**Duração do impacto:** ~1 semana (degradação gradual + picos intermitentes)  
**Severidade:** Alta  
**Status:** Mitigado (índices, cache, JVM); cold start Railway pendente de config

---

## Resumo (TL;DR)

O código da API mede **~53 ms p95 agregado em local**, mas em produção no Railway os mesmos endpoints públicos chegaram a **5–40 s** (cold) e **~500 ms–5 s** (warm). Não foi regressão de um commit — foi **dívida operacional** (cache fraco, índices faltando, JVM default, scale-to-zero) que estourou quando o beta cresceu.

---

## Impacto no cliente

- Login e abertura do app **demorando vários segundos** ou parecendo travado
- Portal web com feature flags e legal docs lentos na primeira carga
- Admin (fdloggers / `security_events`) com queries de **10–20 s** reportadas
- Sensação de “estava rápido e virou lixo do nada” — na verdade degradou aos poucos

### Números (audit 2026-07-01)

| Ambiente | Tier S p95 agregado | `/health` p95 |
|----------|---------------------|---------------|
| Local | **53 ms** | 3 ms |
| Prod warm | **~1.781 ms** | ~1.221 ms |
| Prod cold | até **~40 s** | até **~24 s** |

Fonte: `docs/PERFORMANCE_BASELINE.md`, `docs/RAILWAY_PERFORMANCE.md`

---

## Linha do tempo

| Data | Evento |
|------|--------|
| Jun 2026 | Performance plan no código: cache, N+1, k6, bootstrap (`6f691fd`) |
| Jun–Jul | Beta cresce; tabelas `security_events`, `daily_meal_checkins` acumulam linhas |
| Jul 2026 | Relatos de lentidão extrema em prod; Railway mede P99 20+ s |
| 01/07 | PR #13 Railway: V56 índices + cache TTLs + G1GC (`fcf14fb`) |
| 01/07 | Audit formal prod documentado em `PERFORMANCE_BASELINE.md` |
| 01/07 | Consolidação TTLs/JVM no repo + regras Cursor de latência |

---

## Causa raiz

**Produção não tinha as mesmas condições de performance que o desenvolvimento local** — e ninguém media prod warm de forma contínua antes do beta doer.

### Fatores contribuintes

1. **Cold start Railway (scale-to-zero)**  
   Primeira request após idle paga JVM + Spring Boot (~10–40 s). Quando testávamos com frequência, a instância ficava quente e parecia “rápido”.

2. **Cache da aplicação fraco ou desligado**  
   `CACHE_ENABLED` default false em `application.properties`. Em prod, sem TTLs explícitos, cada abertura de dashboard refazia dezenas de queries MySQL.

3. **Índices compostos ausentes em tabelas que crescem**  
   `security_events` só tinha índice em `user_id`; queries de timeline (`user_id + created_at`) e fdloggers faziam scan pesado.  
   `daily_meal_checkins` sem `(user_id, checkin_date, meal_type)` para adesão.

4. **JVM default no container**  
   Sem `-Xmx2g` e G1GC, GC sob carga beta gerava pausas imprevisíveis.

5. **Mais features = mais requests por sessão**  
   Bootstrap, Pro, evolução, regeneração de plano — código correto, mas **custo total por sessão** subiu sem cache proporcional.

6. **Falsa sensação de segurança do local**  
   Tier S ~50 ms local mascarou que prod nunca foi gate de merge.

---

## O que funcionou

- Audit `perf/audit-endpoints.py` com cold vs warm
- Baseline documentado (`PERFORMANCE_BASELINE.md`)
- Índices V56 + cache TTLs + `docker-entrypoint.sh` com G1GC
- Regras Cursor `api-latency-guardrails.mdc` para não repetir no código

## O que não funcionou

- Assumir que “código rápido local” = prod ok
- Índices só quando a tabela já estava grande
- Deploy sem verificar `CACHE_ENABLED` e réplicas mínimas no Railway

---

## Ações corretivas (feitas)

- [x] Migration `V56__performance_indexes.sql` — `security_events`, checkins, meals, progress_reviews
- [x] `application-prod.properties` — TTLs cache (user-me 300s, profile 600s, checkins 120s, etc.)
- [x] `docker-entrypoint.sh` — `-Xmx2g`, G1GC, `MaxGCPauseMillis=200`
- [x] `docs/RAILWAY_PERFORMANCE.md` — cold start e checklist Railway
- [x] `.cursor/rules/api-latency-guardrails.mdc` — SLAs e gate ×1.2 baseline
- [x] CI nightly k6 (`k6-nightly.yml`)

## Ações preventivas (pendentes)

- [ ] **Min 1 réplica** no Railway (desligar scale-to-zero)
- [ ] Health ping externo a cada 5 min (UptimeRobot / GitHub Actions)
- [ ] Secrets `PERF_TEST_EMAIL/PASSWORD` no GitHub para audit autenticado em CI
- [ ] Bloquear PR se audit warm regredir > ×1.2 vs baseline (workflow opcional)
- [ ] Job de retenção/purge em `security_events` (crescimento infinito)

---

## Lições aprendidas

1. **Latência é requisito de produto** — mesmo feature “correta” que adiciona 3 GETs no dashboard precisa de revisão.
2. **Degradação silenciosa** — tabelas de auditoria e checkins ficam lentas sem ninguém perceber até o volume doer.
3. **Cold start ≠ query lenta** — medir os dois; o usuário culpa “a API” pelos dois somados.
4. **Local não prova prod** — gate de merge deve incluir baseline warm em homolog/prod read-only.
5. **Infra é parte do SLA** — cache, índices, JVM e réplicas mínimas são código de operação, não “depois”.

---

## Material para post

**Título sugerido:** *“Nossa API tinha 53 ms no local e 40 segundos em prod — o que aprendemos”*

**Ângulo:** Não foi bug de lógica; foi o clássico “funciona na minha máquina” com escala, cache e infra. Útil para devs em SaaS early-stage no Railway/planos free.

**3 bullets LinkedIn:**

- Código com p95 de 53 ms local pode estar a 20 s em prod — e tudo pode estar “certo” no repositório.
- Cold start, cache desligado e índice faltando somam; o usuário só vê “travou”.
- Agora todo endpoint Tier S passa por regra de latência + baseline antes de merge.

**Diagrama mental para o post:**

```
Lentidão percebida = cold start (Railway) + queries sem índice + cache miss + GC
```

---

## Referências internas

- `docs/PERFORMANCE.md`
- `docs/PERFORMANCE_BASELINE.md`
- `docs/RAILWAY_PERFORMANCE.md`
- Commit `fcf14fb` (índices Railway), `564e5a7` (consolidação prod)
