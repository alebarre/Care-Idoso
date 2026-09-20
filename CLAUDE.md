# Care Idoso — Regras do Projeto

Este documento consolida os padrões do sistema **Care Idoso**, aplicação web/mobile-first para controle de rotinas de cuidadoras de idoso em plantões de 24h.

## Stacks

- **Frontend:** Angular com NgModules (non-standalone), PrimeNG, FontAwesome, mobile-first.
- **Backend:** Java 21 + Spring Boot 3.3, arquitetura clássica `controller / service / repository`, JPA + Lombok, JWT + Spring Security.
- **Banco:** PostgreSQL 16 com migrations Flyway e Docker Compose.

## Como usar este projeto

Sempre que for trabalhar em um arquivo, carregue a skill correspondente:

- Angular/NgModules/PrimeNG/FontAwesome → `care-angular`
- Spring/JPA/Lombok → `care-spring`
- Segurança → `care-security`
- Docker/Postgres/Flyway → `care-docker`

Use também as skills globais `angular-best-practices` e `angular-best-practices-primeng` como referência complementar. Quando houver conflito, prevalecem as regras deste projeto (`care-*`).

## Regras transversais

1. **Nunca commitar segredos.** Senhas, tokens, chaves JWT e credenciais ficam em `.env` e nunca no código.
2. **Validar entrada.** Todos os DTOs e entidades usam validação com mensagens em português.
3. **Proteger dados sensíveis.** Não logar senhas, tokens ou PII. CORS restrito. Endpoints protegidos por role.
4. **Mobile-first.** Telas pequenas primeiro; usar `mobile-container`, flex/grid, componentes empilhados.
5. **UX estilo Uber, sempre.** Toda tela e componente novo deve seguir a estética "Uber app": `.uber-button`, `.uber-field`/`.uber-label`/`.uber-input`, `.page-screen`/`.page-header`/`.page-content`, `.empty-state` — fundo preto/branco, bordas arredondadas, tipografia limpa, toolbar com botões claros. Nunca fugir desse padrão visual sem pedido explícito do usuário.
6. **Migrations versionadas.** Alterações no banco só via Flyway. Seeds de domínio em migrations; usuários de teste via `SeedRunner` no perfil `dev`.
7. **Controller/Service/Repository.** Lógica de negócio fica em `service`. Controllers apenas recebem DTOs e retornam responses.
8. **Lombok + JPA.** Entidades usam `@Entity`, validações, `@Getter`, `@Setter`, `@Builder`, `@ToString(exclude = ...)` para evitar loops.
9. **Segurança da informação.** Seguir as regras de `care-security` para cada alteração que toque autenticação, dados pessoais ou configuração.

## Estratégia de logs

- **Correlation ID:** `CorrelationIdFilter` (backend) gera ou reaproveita um `correlationId` por requisição (header `X-Correlation-Id`), guarda no MDC e devolve no header de resposta. O frontend gera o UUID no `CorrelationIdInterceptor`, envia no header e usa o mesmo id no `LoggerService`.
- **Backend (SLF4J/Logback, `logback-spring.xml`):**
  - Perfil `dev`: console colorido e legível, com `correlationId` visível no padrão de log.
  - Perfil `prod` (ou qualquer perfil diferente de `dev`): JSON estruturado via `logstash-logback-encoder`, com campos `timestamp`, `level`, `correlationId`, `logger`, `message`, `trace` — pronto para Datadog/Loki/CloudWatch.
- **Sanitização:** e-mails logados sempre passam por `LogSanitizer.mascararEmail(...)`. Nunca logar senha, token JWT, código de verificação (OTP) ou dados de saúde — nem mesmo em `DEBUG`.
- **Frontend (Angular):**
  - `LoggerService` (`core/services`) centraliza `console.*`; em produção, envia apenas erros críticos e anonimizados para o backend de telemetria.
  - `CorrelationIdInterceptor` injeta o header `X-Correlation-Id` em toda requisição HTTP.
  - `ErrorHandler` global (`AppErrorHandler`) captura erros não tratados e loga via `LoggerService`, sem incluir dados sensíveis do usuário.
  - Comportamento (nível mínimo de log, endpoint de telemetria) configurado em `environment.ts` / `environment.prod.ts`.

## Perfis de acesso

- `ADMIN`: acesso total, incluindo gerenciamento de usuários.
- `CUIDADORA`: acesso restrito — pode registrar rotinas, medições, medicações, observações, pedidos e visualizar lembretes.

## Usuários de teste

Criados automaticamente no perfil `dev`:

- `admin@teste.com` / `admin123` (ADMIN)
- `cuidadora@teste.com` / `cuidadora123` (CUIDADORA)
