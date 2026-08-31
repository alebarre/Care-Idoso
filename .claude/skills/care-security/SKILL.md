---
name: care-security
description: Segurança da informação para o Care Idoso. Proteção de credenciais, dados sensíveis, JWT, CORS, validação e privacidade.
version: 1.0.0
tags:
  - security
  - jwt
  - privacy
  - care-idoso
globs:
  - "backend/**/*.java"
  - "frontend/**/*.ts"
  - "frontend/**/*.html"
  - "docker-compose.yml"
  - ".env*"
  - "CLAUDE.md"
---

# Care Idoso — Skill de Segurança da Informação

Use esta skill sempre que uma alteração envolver autenticação, autorização, dados pessoais, saúde, credenciais, chaves ou configuração de infraestrutura.

## Checklist obrigatório

- [ ] Nenhum segredo (senha, token, chave JWT, credencial) foi commitado no código.
- [ ] Senhas são criptografadas com BCrypt antes de persistir.
- [ ] JWT usa secret forte via variável de ambiente (`JWT_SECRET`) e expiração configurável.
- [ ] Endpoints estão protegidos por roles (`ADMIN`, `CUIDADORA`) no backend.
- [ ] CORS está restrito a origens permitidas (não `*`).
- [ ] Dados sensíveis (senhas, tokens, PII, dados de saúde) não são logados.
- [ ] Erros de produção não expõem stack trace.
- [ ] Entrada de usuário é validada e sanitizada.
- [ ] Comunicação em produção usa HTTPS.
- [ ] `.env` está no `.gitignore`; `.env.example` não contém valores reais.

## Dados pessoais e saúde

- Tratar nome, e-mail, condições, medições, medicações e observações como dados sensíveis.
- Não expor esses dados em logs ou mensagens de erro genéricas.
- Limitar retorno de endpoints apenas ao necessário (princípio do menor privilégio).

## Configuração

- `server.error.include-stacktrace=never`
- `careidoso.cors.allowed-origins=${CORS_ALLOWED_ORIGINS}`
- `careidoso.jwt.secret=${JWT_SECRET}`
- `spring.jpa.hibernate.ddl-auto=validate` (nunca `create-drop` em prod).

## Docker

- PostgreSQL não deve ser exposto publicamente em produção.
- Senha do banco em `.env`, nunca hardcoded no `docker-compose.yml`.
