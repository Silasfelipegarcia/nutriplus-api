# Postmortem: Deploy quebrado — V57 com sintaxe PostgreSQL no MySQL

**Data do incidente:** 2026-07-01  
**Duração do impacto:** até próximo deploy bem-sucedido (~30–60 min)  
**Severidade:** Alta (API fora do ar — 100% dos requests)  
**Status:** Resolvido (`ec6034f`)

---

## Resumo (TL;DR)

A migration `V57__nutritionist_profile_enrichment.sql` usou `BIGSERIAL` (PostgreSQL) em banco **MySQL**. Flyway falhou no deploy → `entityManagerFactory` não subiu → Spring não injeta repositórios JPA → **Tomcat não inicia**. A esteira e prod ficaram vermelhos.

---

## Impacto no cliente

- API indisponível (5xx / connection refused)
- App e portal não autenticam
- Nenhum dado corrompido — falha **antes** de aceitar tráfego

### Erro visível nos logs

```
Cannot resolve reference to bean 'jpaSharedEM_entityManagerFactory'
  → securityEventRepository
  → securityRiskService
  → riskEvaluationFilter
  → Tomcat context failed to start
```

Isso é **efeito cascata**, não a causa. A causa está algumas linhas acima: `FlywayException` na V57.

---

## Linha do tempo

| Horário | Evento |
|---------|--------|
| 01/07 ~19:17 | Push MVP Pro: `69ad532` + `564e5a7` (V57 nutritionist) |
| 01/07 ~19:21 | Railway build ok, **startup falha** no Flyway |
| 01/07 ~19:25 | Logs mostram UnsatisfiedDependencyException em cadeia JPA |
| 01/07 | Fix `ec6034f`: V57 reescrita MySQL + idempotente |
| 01/07 | Script `scripts/repair-flyway-v57.sql` para limpar `success=0` |

---

## Causa raiz

**Migration escrita com dialect PostgreSQL em projeto 100% MySQL.**

Trecho problemático:

```sql
-- ❌ PostgreSQL
id BIGSERIAL PRIMARY KEY,
```

Deveria ser:

```sql
-- ✅ MySQL
id BIGINT AUTO_INCREMENT PRIMARY KEY,
```

### Fator contribuinte

- Nenhum teste de integração Flyway rodou no CI antes do merge (Testcontainers/Docker indisponível no pipeline ou não gate obrigatório)
- Revisão humana não cruzou com migrations vizinhas (`V17` usa `AUTO_INCREMENT`)

### Efeito colateral possível

Se o Flyway executou o `ALTER TABLE nutritionists` **antes** do `CREATE TABLE` falhar (DDL auto-commit no MySQL), prod pode ter colunas novas sem tabela de portfólio. Por isso a V57 corrigida é **idempotente** (`information_schema` + `IF NOT EXISTS`).

---

## Ações corretivas (feitas)

- [x] Reescrever `V57__nutritionist_profile_enrichment.sql` em sintaxe MySQL
- [x] Migration idempotente para redeploy após falha parcial
- [x] `scripts/repair-flyway-v57.sql` — `DELETE FROM flyway_schema_history WHERE version='57' AND success=0`
- [x] Postmortem documentado

## Ações preventivas (pendentes)

- [ ] CI obrigatório: `mvn test` com Testcontainers (Flyway sobe todas migrations)
- [ ] Checklist PR: “nova migration segue padrão MySQL do `V17`?”
- [ ] Regra Cursor: proibir `BIGSERIAL`, `SERIAL`, `RETURNING` em `db/migration/*.sql`
- [ ] Smoke pós-deploy: `GET /health` 200 em prod antes de marcar deploy ok

---

## Lições aprendidas

1. **Erro de JPA no startup quase sempre é Flyway ou DB URL** — subir o log até `FlywayException`.
2. **Copiar SQL de tutoriais Postgres quebra MySQL** — sempre espelhar migration existente do mesmo repo.
3. **DDL parcial em MySQL** — migration falha no meio pode deixar schema pela metade; idempotência importa.
4. **Esteira verde no build ≠ API no ar** — health check pós-deploy é obrigatório.

---

## Material para post

**Título sugerido:** *“Um BIGSERIAL derrubou nossa API em produção”*

**Ângulo:** Humor + lição: o stack trace de Spring parece complexo, mas a causa foi uma linha SQL. Bom para devs que misturam dialects.

**3 bullets LinkedIn:**

- `entityManagerFactory` missing no log? Olhe o Flyway primeiro.
- `BIGSERIAL` é PostgreSQL; nosso MySQL quer `AUTO_INCREMENT`.
- Deploy quebrado sem dado corrompido — mas 100% downtime até o fix.

---

## Referências

- Fix: commit `ec6034f`
- Recovery: `docs/FLYWAY_V38_RECOVERY.md` (padrão similar V38)
- `scripts/repair-flyway-v57.sql`
