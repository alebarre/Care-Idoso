---
name: care-angular-rules
description: Regras do frontend Angular do Care Idoso (NgModules, PrimeNG, FontAwesome, mobile-first, design system Uber).
globs:
  - "frontend/**/*.ts"
  - "frontend/**/*.html"
  - "frontend/**/*.scss"
---

# Care Idoso — Frontend Angular

## Arquitetura

- Usar **NgModules** (non-standalone). Cada funcionalidade é um módulo com routing.
- Componentes declarados em `declarations`, nunca em `imports`.
- Preferir lazy-loading para módulos de funcionalidade.
- Serviços compartilhados usam `providedIn: 'root'`.

## Componentes

- Nomear arquivos com padrão: `nome.component.ts`, `nome.component.html`, `nome.component.scss`.
- Usar `standalone: false` no `@Component`.
- Inicializar `FormGroup` no `ngOnInit`, não em campo inline.
- Evitar `any`; criar interfaces em `core/models/`.

## UI

- Design System global em `frontend/src/styles.scss` (tokens de cores, tipografia, espaçamento, botões, inputs, cards, listas).
- FontAwesome via `FaIconLibrary` no `SharedModule`.
- Layout mobile-first: `mobile-container`, cards empilhados, toolbar com botões claros.
- Botão primário: `.uber-button` (fundo preto, texto branco, arredondado, 52px altura, full-width).
- Campo de texto: `.uber-field` + `.uber-label` + `.uber-input` (fundo cinza, borda preta no foco, 52px altura).
- Telas internas: `.page-screen` + `.page-header` com `.icon-button` de voltar, título centralizado, `.page-content`.
- Estado vazio: `.empty-state` com ícone e duas linhas.
- Toast e mensagens de erro centralizadas via `MessageService`.

## Requisições

- `HttpClientModule` importado uma única vez no `AppModule`.
- JWT interceptado via `JwtInterceptor`.
- Guards `AuthGuard` e `AdminGuard` protegem rotas.

## Proxy

- Requisições para `/api` são redirecionadas por `proxy.conf.json` para `http://localhost:8080`.
