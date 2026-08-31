---
name: care-spring
description: Backend Spring Boot do Care Idoso. Controller/Service/Repository, JPA, Lombok, validações, DTOs records, JWT e roles.
version: 1.0.0
tags:
  - spring-boot
  - java
  - jpa
  - lombok
  - care-idoso
globs:
  - "backend/**/*.java"
  - "backend/**/*.yml"
  - "backend/**/*.sql"
  - "backend/pom.xml"
---

# Care Idoso — Skill Spring Boot

Use esta skill ao gerar ou modificar código do backend Java/Spring do Care Idoso.

## Arquitetura

- Estrutura clássica: `controller` → `service` → `repository`.
- Controllers expõem endpoints REST sob `/api/**`.
- Services contêm regras de negócio e acesso a dados.
- Repositories são interfaces Spring Data JPA.

## Entidades (JPA + Lombok)

- `@Entity`, `@Table`, `@Id`, `@GeneratedValue`.
- Lombok: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.
- Validations do Jakarta com mensagens em português.
- `@ToString(exclude = ...)` para evitar loops em relacionamentos.
- Relacionamentos `@ManyToOne(fetch = FetchType.LAZY)`.

## DTOs

- `record` para request/response.
- Validações no DTO de request.
- Nunca expor a senha em responses.

## Segurança

- JWT stateless (`JwtService`, `JwtFilter`, `SecurityConfig`).
- Roles `ADMIN` e `CUIDADORA`.
- BCrypt para senhas.

## Exceções

- `GlobalExceptionHandler` centraliza erros.
- Mensagens amigáveis; stack trace oculto em produção.

## Migrations

- Flyway: arquivos `V{num}__nome_descritivo.sql` em `db/migration`.
- DDL na V1; dados iniciais de domínio em V2+; usuários de teste via `SeedRunner` no perfil `dev`.
