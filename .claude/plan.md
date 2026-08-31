# Plano de implementação — Care Idoso

## 1. Objetivo
Criar a estrutura inicial de uma aplicação web/mobile-first para controle de rotinas de cuidadoras de idoso em plantões de 24h, com:
- Frontend Angular (non-standalone), PrimeNG e FontAwesome.
- Backend Java/Spring clássico (controller/service/repository), JPA + Lombok.
- PostgreSQL com migrations (Flyway) via Docker.
- Roles ADMIN e CUIDADORA.
- Rules e skills do Claude para codificar os padrões do projeto.
- README com instruções completas de instalação, seeds e usuários de teste.

## 2. Estrutura de pastas

```
/home/alebarre/Projetos/care-idoso/
├── README.md
├── docker-compose.yml
├── .env.example
├── .env
├── .claude/
│   ├── CLAUDE.md                      # regras gerais do projeto
│   ├── rules/
│   │   ├── angular-non-standalone.md
│   │   ├── spring-backend.md
│   │   ├── jpa-lombok-validation.md
│   │   └── security-data-protection.md
│   └── skills/
│       ├── care-angular/SKILL.md
│       ├── care-spring/SKILL.md
│       ├── care-security/SKILL.md
│       └── care-docker/SKILL.md
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/careidoso/
│       ├── CareIdosoApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── CorsConfig.java
│       │   └── JwtConfig.java
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── UsuarioController.java
│       │   ├── IdosoController.java
│       │   ├── MedicamentoController.java
│       │   ├── MedicacaoController.java
│       │   ├── MedicaoController.java
│       │   ├── ObservacaoController.java
│       │   ├── LembreteController.java
│       │   └── PedidoController.java
│       ├── service/
│       │   ├── AuthService.java
│       │   ├── JwtService.java
│       │   ├── UsuarioService.java
│       │   ├── IdosoService.java
│       │   ├── MedicamentoService.java
│       │   ├── MedicacaoService.java
│       │   ├── MedicaoService.java
│       │   ├── ObservacaoService.java
│       │   ├── LembreteService.java
│       │   └── PedidoService.java
│       ├── repository/
│       │   └── (interfaces Spring Data JPA)
│       ├── model/
│       │   ├── Usuario.java
│       │   ├── Perfil.java            # enum ADMIN / CUIDADORA
│       │   ├── Idoso.java
│       │   ├── Medicamento.java
│       │   ├── Medicacao.java
│       │   ├── Medicao.java
│       │   ├── TipoMedicao.java       # enum GLICEMIA, PRESSAO, TEMPERATURA
│       │   ├── Observacao.java
│       │   ├── Lembrete.java
│       │   ├── Pedido.java
│       │   └── PedidoItem.java
│       ├── dto/
│       │   └── (records de request/response)
│       ├── exception/
│       │   └── GlobalExceptionHandler.java
│       └── security/
│           ├── JwtFilter.java
│           └── UserDetailsImpl.java
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── db/migration/
│           ├── V1__estrutura_inicial.sql
│           └── V2__seeds_usuarios.sql
└── frontend/
    ├── angular.json
    ├── package.json
    ├── Dockerfile
    └── src/app/
        ├── app.module.ts
        ├── app-routing.module.ts
        ├── core/
        │   ├── auth/
        │   ├── guards/
        │   ├── interceptors/
        │   └── models/
        ├── modules/
        │   ├── dashboard/
        │   ├── login/
        │   ├── idosos/
        │   ├── rotinas/
        │   ├── medicamentos/
        │   ├── lembretes/
        │   ├── pedidos/
        │   └── usuarios/
        ├── shared/
        │   ├── components/
        │   ├── primeng-config.ts
        │   └── fontawesome-config.ts
        └── environments/
```

## 3. Decisões técnicas

### 3.1 Frontend
- **Angular 17+ com NgModules**: criado via `ng new frontend --routing --style=scss --ssr=false --no-standalone`.
- **PrimeNG 17+**: importar módulos de componentes no `SharedModule`.
- **FontAwesome**: via `@fortawesome/angular-fontawesome`, configuração central no `SharedModule`.
- **Mobile-first**: layout responsivo com CSS Grid/Flexbox, cards empilhados em telas pequenas.
- **Autenticação**: JWT armazenado em memória (service), login via `/api/auth/login`.
- **Interceptors**: adiciona token JWT, trata erros 401/403.
- **Guards**: `AuthGuard` (logado) e `RoleGuard` (admin).

### 3.2 Backend
- **Spring Boot 3.3+ / Java 21**.
- **Dependências**: Spring Web, Spring Data JPA, Spring Security, Spring Validation, Flyway, PostgreSQL Driver, Lombok, JWT (jjwt).
- **Autenticação**: JWT stateless com login/senha criptografada (BCrypt).
- **Roles**: `ADMIN` (acesso total) e `CUIDADORA` (acesso restrito).
- **DTOs**: records Java para request/response.
- **Models JPA**: anotações `Entity`, `Table`, `Id`, `GeneratedValue`, `ManyToOne`, `@NotBlank`, `@Size`, etc., com mensagens de erro em português.
- **Lombok**: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.
- **Controller/Service/Repository**: estrutura clássica, sem lógica de negócio no controller.

### 3.3 Banco de dados
- **PostgreSQL 16** via Docker Compose.
- **Flyway** gerencia migrations.
- **Seeds**: usuários de teste e dados mínimos inseridos por migrations.

### 3.4 Segurança
- JWT secret via variável de ambiente (`JWT_SECRET`), nunca hardcoded.
- Senhas criptografadas com BCrypt.
- CORS configurado apenas para origens permitidas.
- Endpoints protegidos por role.
- Não expor stack trace em erros de produção.
- Não logar senhas, tokens ou dados sensíveis.
- Sanitização de entrada (validation) e parâmetros de busca.
- Variáveis sensíveis em `.env` e `.env.example` sem valores reais.

### 3.5 Rules e skills
- Reutilizar skills globais existentes (`angular-best-practices`, `angular-best-practices-primeng`) onde não conflitarem.
- Criar skills locais específicas do domínio:
  - `care-angular`: NgModules, PrimeNG modules, FontAwesome, mobile-first.
  - `care-spring`: controller/service/repository, JPA, Lombok, JWT.
  - `care-security`: proteção de dados sensíveis, chaves, senhas, tokens.
  - `care-docker`: PostgreSQL + Flyway + Docker Compose.
- Criar rules em `.claude/rules/` e `CLAUDE.md` para consolidar padrões.

## 4. Migrations e seeds
- `V1__estrutura_inicial.sql`: cria todas as tabelas, chaves estrangeiras, constraints.
- `V2__seeds_usuarios.sql`: insere usuários de teste e dados mínimos.

Usuários de teste:
- admin@teste.com / admin123 (ADMIN)
- cuidadora@teste.com / cuidadora123 (CUIDADORA)

## 5. README
README.md na raiz conterá:
- Visão geral.
- Pré-requisitos (Docker, Maven, Node 20+, Angular CLI).
- Passos para clonar e configurar `.env`.
- Subir banco: `docker compose up -d`.
- Rodar backend: `./mvnw spring-boot:run`.
- Rodar frontend: `npm install && ng serve`.
- Migrations automáticas via Flyway.
- Usuários de teste.
- URLs de acesso.

## 6. Entregáveis desta fase
1. Estrutura completa de pastas e arquivos iniciais.
2. `pom.xml` do Spring Boot com todas as dependências.
3. `angular.json`, `package.json` e estrutura Angular non-standalone.
4. Docker Compose com PostgreSQL.
5. Primeiras migrations e seeds.
6. Skills e rules do Claude.
7. README.md com instruções.

## 7. Próximos passos após aprovação
Após aprovação do plano, seguir com a geração dos arquivos listados acima, começando pelas configurações e estruturas, depois implementando entidades, segurança, módulos iniciais e README.
