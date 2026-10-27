# Care Idoso — Padrão de Comentários

Vale para todo código novo ou alterado, em qualquer prompt. Comentários em português.

## Princípios

- Comente o **porquê**, o contrato e os riscos; nunca o que o código já diz.
- Atualize ou remova o comentário no mesmo commit em que o código muda. Comentário desatualizado é pior que nenhum.
- Sem `// TODO` ou `// FIXME` que não aponte para uma issue (ex.: `// TODO(#12): ...`).
- Nunca colocar segredos, senhas, tokens ou e-mails reais em comentários.
- Não comentar código morto: apague-o.

## Back-end (Java)

- JavaDoc em classes e métodos públicos de controllers, services, security, config, entidades e DTOs.
- Controllers e services: documentar regra de negócio, permissões exigidas, efeitos colaterais (e-mail, revogação de tokens, arquivos) e exceções lançadas.
- Entidades: documentar constraints, relacionamentos e invariantes não óbvios (ex.: token de uso único).
- DTOs (`record`): propósito e regras de validação; `@param` quando o nome do campo não bastar.
- Código de segurança (`JwtFilter`, `SecurityConfig`, `JwtService`): explicar a decisão e a ameaça que ela mitiga, de forma direta e técnica.
- Migrations Flyway: um comentário `--` no topo com a intenção. Nunca alterar o SQL de migration já aplicada.

## Front-end (Angular/TypeScript)

- TSDoc em serviços, interceptors, guards, handlers, componentes e interfaces de `core/models`.
- Componentes: propósito, `@Input`/`@Output` e efeitos colaterais (navegação, `localStorage`, chamadas HTTP).
- Serviços e interceptors: contrato, fluxo de erro e regras não triviais (ex.: refresh de token, fila de requisições).
- `styles.scss`: comentário de seção para tokens, componentes do Design System e breakpoints.
- Templates HTML: comentar apenas o que não for evidente.
