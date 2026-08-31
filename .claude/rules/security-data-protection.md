---
name: care-security-rules
description: Regras de segurança da informação para o Care Idoso.
globs:
  - "backend/**/*.java"
  - "frontend/**/*.ts"
  - "frontend/**/*.html"
  - "docker-compose.yml"
  - ".env*"
---

# Care Idoso — Segurança da Informação

## Credenciais e segredos

- Nunca commitar senhas, tokens, chaves JWT, credenciais de banco ou certificados.
- Armazenar segredos em `.env` e carregar via Spring properties.
- Usar `.env.example` sem valores reais como referência.

## Senhas

- Armazenar senhas com BCrypt (nunca texto plano ou hash fraco).
- Validar tamanho mínimo e complexidade no cadastro.

## Autenticação

- JWT stateless com secret forte e rotação periódica em produção.
- Token enviado no header `Authorization: Bearer <token>`.
- Expiração configurável via `JWT_EXPIRATION_MS`.

## Autorização

- Endpoints sensíveis protegidos por roles (`ADMIN`, `CUIDADORA`).
- Validar permissão no backend; nunca confiar apenas no frontend.

## Dados sensíveis

- Não logar senhas, tokens, e-mails ou informações de saúde.
- Sanitizar entrada de dados (validation) e parâmetros de URL.
- CORS configurado apenas para origens permitidas.

## Comunicação

- Em produção, exigir HTTPS.
- Não expor stack trace ou detalhes internos em erros de produção.

## Docker

- Banco não exposto para a internet; porta mapeada apenas em dev.
- Senha do PostgreSQL em `.env`, nunca no `docker-compose.yml` em texto plano.
