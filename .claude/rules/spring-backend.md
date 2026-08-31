---
name: care-spring-rules
description: Regras do backend Spring Boot do Care Idoso (controller/service/repository, JPA, Lombok, JWT).
globs:
  - "backend/**/*.java"
  - "backend/**/*.yml"
  - "backend/**/*.sql"
---

# Care Idoso — Backend Spring Boot

## Arquitetura

- Estrutura clássica: `controller` → `service` → `repository`.
- Controllers recebem DTOs (records) e retornam DTOs/entidades.
- Services contêm toda a lógica de negócio e regras de acesso.
- Repositories são interfaces Spring Data JPA.

## Autenticação e autorização

- JWT stateless.
- `SecurityConfig` protege endpoints por role (`ADMIN`, `CUIDADORA`).
- `JwtFilter` valida token em cada requisição.
- Senhas criptografadas com `BCryptPasswordEncoder`.

## Dados

- Entidades em `model/`, com anotações JPA e Lombok.
- DTOs em `dto/request/` e `dto/response/` como records.
- Validações em português nos DTOs e entidades.
- Migrations Flyway em `db/migration/`.

## Configuração

- Segredos e variáveis sensíveis via `.env` e `application-*.yml`.
- Perfil `dev` com seeds automáticos e SQL visível.
- Nunca expor stack trace em produção.
