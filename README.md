# Nutri+ — Backend Java

API REST Spring Boot para autenticação, perfil nutricional e orquestração do agente de IA.

Repositório: **nutriplus-api**

## Pré-requisitos

- Java 21+
- Maven 3.9+
- MySQL 8+
- Agente Python rodando em `http://localhost:8000` (repositório `nutriplus-agentes`)

## Configuração

Usa o **mesmo MySQL local** do Hosty (`127.0.0.1:3306`), com banco separado `nutriplus` (o Hosty usa `hosty_app`).

1. Crie o banco (se ainda não existir):

```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS nutriplus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

2. Aplique o schema (recomendado na primeira vez):

```bash
mysql -u root < docs/schema.sql
```

3. Variáveis de ambiente (opcional — os defaults já seguem o padrão do Hosty):

```bash
export DB_URL="jdbc:mysql://127.0.0.1:3306/nutriplus?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true"
export DB_USERNAME=root
export DB_PASSWORD=
export JWT_SECRET="sua-chave-secreta-com-pelo-menos-32-caracteres"
export AI_AGENT_URL="http://localhost:8000"
```

> Com profile `dev`, o Hibernate usa `ddl-auto: update` e cria/atualiza tabelas automaticamente. Sem `dev`, use o `docs/schema.sql` e `ddl-auto: validate`.

> **Docker:** `docker compose up -d` só é necessário se você *não* tiver MySQL local rodando.

## Executar

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

API disponível em `http://localhost:8080`.

## Endpoints

| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/auth/register` | Cadastro |
| POST | `/auth/login` | Login |
| GET | `/users/me` | Usuário autenticado |
| POST | `/nutrition-profile` | Salvar perfil nutricional |
| GET | `/nutrition-profile` | Obter perfil |
| POST | `/meal-plans/generate` | Gerar plano alimentar |
| GET | `/meal-plans/latest` | Último plano |
| GET | `/shopping-list/latest` | Última lista de compras |

## Repositórios relacionados

| Repositório | Função |
|-------------|--------|
| [nutriplus-agentes](../nutriplus-agentes) | Agente Python (IA + cálculos) |
| [nutriplus-frontend](../nutriplus-frontend) | App Flutter |
