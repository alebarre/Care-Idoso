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
5. **Migrations versionadas.** Alterações no banco só via Flyway. Seeds de domínio em migrations; usuários de teste via `SeedRunner` no perfil `dev`.
6. **Controller/Service/Repository.** Lógica de negócio fica em `service`. Controllers apenas recebem DTOs e retornam responses.
7. **Lombok + JPA.** Entidades usam `@Entity`, validações, `@Getter`, `@Setter`, `@Builder`, `@ToString(exclude = ...)` para evitar loops.
8. **Segurança da informação.** Seguir as regras de `care-security` para cada alteração que toque autenticação, dados pessoais ou configuração.

## Perfis de acesso

- `ADMIN`: acesso total, incluindo gerenciamento de usuários.
- `CUIDADORA`: acesso restrito — pode registrar rotinas, medições, medicações, observações, pedidos e visualizar lembretes.

## Usuários de teste

Criados automaticamente no perfil `dev`:

- `admin@teste.com` / `admin123` (ADMIN)
- `cuidadora@teste.com` / `cuidadora123` (CUIDADORA)
