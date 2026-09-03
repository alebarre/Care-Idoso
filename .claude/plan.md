# Plano de implementação — Care Idoso

## Issue em foco
**#1 — [Feature] Esqueci a senha e Nova conta com validação por código de 5 dígitos por e-mail**

## Objetivo
Implementar os fluxos de **"Esqueci a senha"** e **"Criar nova conta"** com código numérico de 5 dígitos enviado por e-mail, TTL de 1 minuto, one-time, seguindo os padrões de backend, frontend e segurança do projeto.

## Decisões e premissas

1. **Conta nova**: o usuário será criado no endpoint `/api/auth/nova-conta` com `ativo = false` e só poderá fazer login após validar o código (`ativo = true`).
2. **Código de verificação**: armazenado na tabela `codigos_verificacao` (migration `V3`), com expiração de 1 minuto e flag `utilizado`.
3. **Reenvio**: permitido apenas após expiração do código anterior — rate limit simples por e-mail + tipo (cooldown de 60 segundos).
4. **Senhas**: mantemos BCrypt. Nestes fluxos exigiremos **mínimo 6 caracteres** e **letras + números** (compatível com os seeds `admin123` e `cuidadora123`).
5. **E-mail**: envio via **Spring Mail** (`JavaMailSender`) com HTML inline, estilo Uber, cores distintas para redefinição de senha e nova conta.
6. **Segurança**: nunca logar código, tokens ou senhas. CORS e permissões dos endpoints `/api/auth/**` já estão corretos.
7. **Frontend**: páginas `/login/esqueci-senha` e `/login/nova-conta` dentro do módulo `login`, com 3 passos (formulário → código → conclusão), contador regressivo de 1 minuto e reenvio.

## Tarefas de backend

1. **Dependência** — adicionar `spring-boot-starter-mail` no `pom.xml`.
2. **Configuração** — adicionar propriedades `spring.mail.*` em `application.yml` lendo das variáveis de `.env`.
3. **Banco** — criar migration `V3__codigos_verificacao.sql` com a tabela especificada e índices para `(email, tipo, utilizado, expiracao)`.
4. **Modelo** — criar `TipoCodigoVerificacao` (enum) e entidade `CodigoVerificacao` com JPA + Lombok + validações.
5. **Repository** — criar `CodigoVerificacaoRepository` com métodos de busca por e-mail/tipo/utilizado/expiração.
6. **E-mail** — criar `EmailService` com templates HTML inline para redefinição de senha (preto/branco) e nova conta (verde sutil).
7. **Serviço de códigos** — criar `CodigoVerificacaoService` para gerar, validar, invalidar e aplicar cooldown.
8. **DTOs** — criar `EsqueciSenhaRequest`, `RedefinirSenhaRequest`, `NovaContaRequest` e `ValidarCodigoRequest`, todos com mensagens em português.
9. **AuthService** — adicionar:
   - `solicitarRedefinicaoSenha(email)`
   - `redefinirSenha(email, codigo, novaSenha)`
   - `solicitarNovaConta(dados)`
   - `ativarNovaConta(email, codigo)`
10. **AuthController** — expor:
    - `POST /api/auth/esqueci-senha`
    - `POST /api/auth/validar-codigo-redefinicao`
    - `POST /api/auth/nova-conta`
    - `POST /api/auth/validar-codigo-nova-conta`
11. **Tratamento de erros** — adicionar handlers específicos em `GlobalExceptionHandler` se necessário.

## Tarefas de frontend

1. **Login** — adicionar links "Esqueci a senha" e "Criar nova conta" na tela de login.
2. **Rotas** — criar `/login/esqueci-senha` e `/login/nova-conta` no `login-routing.module.ts`.
3. **Componentes** — criar `EsqueciSenhaComponent` e `NovaContaComponent` no módulo `login`.
4. **Modelos** — criar interfaces `EsqueciSenhaRequest`, `RedefinirSenhaRequest`, `NovaContaRequest`, `ValidarCodigoRequest`.
5. **AuthService** — adicionar métodos para os novos endpoints.
6. **Telas** — implementar wizard de 3 passos usando o design system existente (`.page-screen`, `.page-header`, `.icon-button`, `.uber-field`, `.uber-input`, `.uber-button`).
7. **Código** — input centralizado de 5 dígitos e contador regressivo com reenvio.
8. **Build** — garantir que `ng build` passe sem erros.

## Configuração e entrega

1. Revisar `.env.example` para garantir todas as variáveis de e-mail documentadas.
2. Adicionar ao `README` as variáveis de e-mail necessárias.
3. Validar builds do backend (`./mvnw clean verify`) e frontend (`npm run build`).
4. Verificar critérios de aceitação da issue #1.

## Skills a carregar durante a implementação

- `care-spring` para backend.
- `care-angular` para frontend.
- `care-security` para dados sensíveis, senhas e tokens.
- `care-docker` se for necessário ajustar Docker/Flyway.
