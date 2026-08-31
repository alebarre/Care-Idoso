---
name: care-docker
description: Docker Compose, PostgreSQL e Flyway para o Care Idoso.
version: 1.0.0
tags:
  - docker
  - postgresql
  - flyway
  - care-idoso
globs:
  - "docker-compose.yml"
  - "backend/src/main/resources/db/migration/**/*.sql"
  - "backend/src/main/resources/application*.yml"
  - ".env*"
---

# Care Idoso — Skill Docker/Postgres/Flyway

Use esta skill ao alterar configuração de banco, Docker Compose, migrations ou variáveis de ambiente.

## Docker Compose

- Serviço `postgres` com imagem `postgres:16-alpine`.
- Porta mapeada via `${DB_PORT:-5432}`.
- Credenciais via variáveis de ambiente (`DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`).
- Volume persistente `postgres_data`.

## Banco de dados

- PostgreSQL 16.
- Charset/recomendação: UTF-8.
- Healthcheck com `pg_isready`.

## Flyway

- Migrations em `backend/src/main/resources/db/migration/`.
- Nomenclatura: `V{numero}__descricao.sql`.
- `baseline-on-migrate: true` no `application.yml`.
- `spring.flyway.enabled=true`.

## Seeds

- Dados de domínio (idosos, medicamentos, lembretes) via migrations versionadas.
- Usuários de teste via `SeedRunner` no perfil `dev`.

## Variáveis de ambiente

- `.env` na raiz do projeto.
- `.env.example` com chaves e valores fictícios de exemplo.
- Nunca commitar `.env` real.

## Comandos úteis

```bash
# Subir banco
docker compose up -d

# Parar
docker compose down

# Remover dados
docker compose down -v
```
