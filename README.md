# Care Idoso

Aplicação web/mobile-first para controle de rotinas de cuidadoras de idoso que se revezam em plantões de 24 horas.

## Funcionalidades

- Registro de rotinas por cuidadora (plantão contínuo).
- Controle de medicações com dosagem, horário e via de administração.
- Medições de glicemia, pressão arterial e temperatura.
- Observações gerais sobre o idoso.
- Lembretes de horários de remédios.
- Lista de pedidos de medicamentos/produtos em falta.
- Perfis de acesso: **ADMIN** (acesso total) e **CUIDADORA** (acesso restrito).

## Stacks

- **Frontend:** Angular (non-standalone / NgModules), PrimeNG, FontAwesome.
- **Backend:** Java 21 + Spring Boot 3.3 + Spring Security + JWT + JPA + Lombok.
- **Banco de dados:** PostgreSQL 16.
- **Migrations:** Flyway.
- **Infra local:** Docker Compose.

## Pré-requisitos

- Docker e Docker Compose instalados.
- Java 21 instalado (ou compatível).
- Node.js 20+ e Angular CLI 17+.
- Maven (opcional — o backend já inclui `mvnw`).

## Estrutura

```
care-idoso/
├── backend/            # Spring Boot
├── frontend/           # Angular
├── docker-compose.yml  # PostgreSQL
├── .env.example
└── README.md
```

## Como subir o projeto

### 1. Banco de dados (PostgreSQL)

Na raiz do projeto, inicie o banco:

```bash
docker compose up -d
```

O container ficará disponível em `localhost:5432`.

### 2. Backend

Entre na pasta do backend:

```bash
cd backend
```

Configure as variáveis de ambiente. O projeto já inclui um `.env` na raiz, mas recomenda-se copiar e ajustar:

```bash
cp ../.env.example ../.env
```

Suba a aplicação:

```bash
./mvnw spring-boot:run
```

Ou, se preferir usar Maven instalado:

```bash
mvn spring-boot:run
```

O backend iniciará em `http://localhost:8080`.

#### Migrations

As migrations do Flyway são executadas automaticamente na inicialização:

- `V1__estrutura_inicial.sql`: cria todas as tabelas.
- `V2__dados_iniciais.sql`: insere idoso, medicamentos e lembretes de exemplo.
- `V3`/`V4`: códigos de verificação (OTP) e ajuste para `TIMESTAMPTZ`.
- `V5__refresh_tokens.sql`: refresh tokens com rotação.
- `V6__usuario_foto_perfil.sql`: campo `foto` em `usuarios`.
- `V7__codigos_verificacao_endurecimento.sql`: OTP de 6 dígitos e contador de tentativas.
- `V8__usuario_exclusao_logica.sql`: campo `excluido` (exclusão lógica de usuários).

Nunca altere uma migration já aplicada (o Flyway valida o checksum, comentários inclusos): crie uma nova versão.

#### Fotos de perfil (`UPLOAD_DIR`)

As fotos de perfil (JPG/PNG, até 2 MB) são gravadas em disco, em `UPLOAD_DIR/fotos` (padrão `./uploads`, relativo ao diretório de execução do backend); o banco guarda apenas o nome do arquivo. O diretório é ignorado pelo Git.

Em produção:

- Aponte `UPLOAD_DIR` para um caminho **fora do código e persistente** (volume Docker ou disco montado). Sem isso as fotos somem a cada novo deploy/contêiner.
- O usuário do processo precisa de permissão de leitura e escrita nesse diretório; ninguém mais deve ter acesso.
- Inclua o diretório na rotina de backup junto com o banco: banco e arquivos precisam ser restaurados da mesma data.
- As fotos não são servidas publicamente: só passam pelo backend, com JWT.

### 3. Frontend

Entre na pasta do frontend:

```bash
cd frontend
```

Instale as dependências:

```bash
npm install
```

Inicie o servidor de desenvolvimento:

```bash
ng serve
```

Acesse `http://localhost:4200`.

## Usuários de teste

Os usuários abaixo são criados automaticamente pelo `SeedRunner` ao iniciar o backend no perfil `dev`:

| E-mail                | Senha          | Perfil      |
|-----------------------|----------------|-------------|
| `admin@teste.com`     | `admin123`     | ADMIN       |
| `cuidadora@teste.com` | `cuidadora123` | CUIDADORA   |

## Principais endpoints

| Recurso                | Endpoint                 | Métodos principais |
|------------------------|--------------------------|--------------------|
| Autenticação           | `/api/auth/login`        | POST               |
| Usuários (ADMIN)       | `/api/usuarios`, `/api/usuarios/{id}`, `/api/usuarios/{id}/status` | GET, POST, PUT, PATCH, DELETE |
| Meu perfil             | `/api/usuarios/me`, `/api/usuarios/me/foto` | GET, PUT, POST |
| Idosos                 | `/api/idosos`            | GET, POST          |
| Medicamentos           | `/api/medicamentos`      | GET, POST          |
| Medicações             | `/api/medicacoes`        | GET, POST          |
| Medições               | `/api/medicoes`          | GET, POST          |
| Observações            | `/api/observacoes`       | GET, POST          |
| Lembretes              | `/api/lembretes`         | GET, POST          |
| Pedidos (faltas)       | `/api/pedidos`           | GET, POST          |

## Segurança

- Autenticação stateless com JWT.
- Senhas armazenadas com BCrypt.
- Roles `ADMIN` e `CUIDADORA` protegem endpoints sensíveis.
- CORS configurado via variável de ambiente.
- Segredos (JWT, senhas) nunca são commitados — mantenhas no `.env`.

## Desenvolvimento

### Perfis do Spring

- `dev` (padrão): CORS permissivo, SQL logado, seeds automáticos.
- Altere `SPRING_PROFILES_ACTIVE` no `.env` para trocar o perfil.

### Parar tudo

```bash
docker compose down
# ou para remover volumes:
docker compose down -v
```

## Licença

Projeto privado — uso interno.
