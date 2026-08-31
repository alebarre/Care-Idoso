---
name: care-angular
description: Frontend Angular do Care Idoso. NgModules (non-standalone), PrimeNG, FontAwesome, mobile-first, interceptors JWT, guards de role.
version: 1.0.0
tags:
  - angular
  - primeng
  - fontawesome
  - mobile-first
  - care-idoso
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

## Padrões obrigatórios

### 1. Componentes

- Arquivos: `nome.component.ts`, `nome.component.html`, `nome.component.scss`.
- Decorador com `standalone: false`.
- Inicializar `FormGroup` no `ngOnInit`.

### 2. Módulos

- Um módulo por funcionalidade, com `*-routing.module.ts` e lazy loading.
- `SharedModule` centraliza PrimeNG, FontAwesome, ReactiveFormsModule e CommonModule.

### 3. Estilos

- Use classes utilitárias do PrimeFlex.
- Container mobile: `mobile-container` (max-width 480px, centralizado).
- Cards empilhados em telas pequenas.

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
