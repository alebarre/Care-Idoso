---
name: care-angular
description: Frontend Angular do Care Idoso. NgModules (non-standalone), PrimeNG, FontAwesome, mobile-first, visual Uber, interceptors JWT, guards de role.
version: 1.1.0
tags:
  - angular
  - primeng
  - fontawesome
  - mobile-first
  - care-idoso
  - uber-ui
globs:
  - "frontend/**/*.ts"
  - "frontend/**/*.html"
  - "frontend/**/*.scss"
  - "frontend/angular.json"
  - "frontend/package.json"
---

# Care Idoso — Skill Angular

Use esta skill ao gerar ou modificar código do frontend Angular do projeto Care Idoso.

## Stack

- Angular 21+ com **NgModules** (non-standalone).
- PrimeNG 21+ (temas via `@primeuix/themes/aura` e `providePrimeNG`).
- FontAwesome via `@fortawesome/angular-fontawesome`.
- Mobile-first, responsivo.
- **Design System inspirado no app Uber**: tipografia Inter, alto contraste preto/branco, botões e inputs arredondados, listas com ícones e chevrons.

## Padrões obrigatórios

### 1. Componentes

- Arquivos: `nome.component.ts`, `nome.component.html`, `nome.component.scss`.
- Decorador com `standalone: false`.
- Inicializar `FormGroup` no `ngOnInit`.

### 2. Módulos

- Um módulo por funcionalidade, com `*-routing.module.ts` e lazy loading.
- `SharedModule` centraliza PrimeNG, FontAwesome, ReactiveFormsModule e CommonModule.

### 3. Estilos

- Design System global em `frontend/src/styles.scss`. Não duplicar tokens; use as classes e variáveis CSS abaixo.
- **Container mobile**: `mobile-container` (max-width 480px, centralizado).
- **Botão primário**: `.uber-button` (fundo preto, texto branco, borda arredondada, altura 52px, full-width).
- **Campo de texto**: `.uber-field` + `.uber-label` + `.uber-input` (fundo cinza #f3f3f3, borda transparente, foco com borda preta, altura 52px).
- **Cards**: `.uber-card`, `.uber-card--flat`.
- **Itens de lista**: `.uber-list-item`.
- **Telas internas**: `.page-screen` + `.page-header` com botão de voltar + título centralizado + `.page-content`.
- **Estado vazio**: `.empty-state` com ícone e duas linhas de texto.
- Ícones circulares em botões/cards via `.icon-button` ou `.dashboard-card__icon`.

### 4. Requisições

- `HttpClientModule` importado no `AppModule`.
- `JwtInterceptor` adiciona o token automaticamente.
- Proxy `/api` para `http://localhost:8080` em desenvolvimento.

### 5. Segurança

- Guards `AuthGuard` e `AdminGuard` protegem rotas.
- Mensagens de erro amigáveis via `MessageService`.

## Anti-padrões

- Não criar componentes standalone.
- Não importar `RouterModule` ou `HttpClientModule` em módulos de funcionalidade (use `SharedModule`).
- Não hardcodar URLs de API.
- Não redefinir tokens de cores/tipografia em componentes; usar variáveis do `styles.scss`.

## Exemplo de módulo

```typescript
@NgModule({
  declarations: [MinhaPageComponent],
  imports: [
    SharedModule,
    MinhaRoutingModule
  ]
})
export class MinhaModule { }
```

## Exemplo de tela interna

```html
<div class="page-screen">
  <header class="page-header">
    <a routerLink="/" class="icon-button" aria-label="Voltar">
      <fa-icon [icon]="['fas', 'arrow-left']"></fa-icon>
    </a>
    <h1 class="heading-md">Título</h1>
    <div class="icon-placeholder"></div>
  </header>

  <section class="page-content">
    <div class="uber-card uber-card--flat">
      <div class="empty-state">
        <fa-icon [icon]="['fas', 'clock']" class="empty-state__icon"></fa-icon>
        <p class="body-md">Mensagem principal.</p>
        <p class="body-sm">Subtexto explicativo.</p>
      </div>
    </div>
  </section>
</div>
```
