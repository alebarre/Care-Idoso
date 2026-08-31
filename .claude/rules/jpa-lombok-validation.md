---
name: care-jpa-lombok-rules
description: Regras para modelos JPA com Lombok e validações no Care Idoso.
globs:
  - "backend/src/main/java/com/careidoso/model/**/*.java"
---

# Care Idoso — Modelos JPA + Lombok

## Estrutura

- Toda entidade tem `@Entity`, `@Table(name = "...")`, `@Id` e `@GeneratedValue`.
- Usar Lombok: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.
- Incluir mensagens de erro em português nas anotações de validação.

## Relacionamentos

- `@ManyToOne(fetch = FetchType.LAZY)` por padrão.
- `@OneToMany` com `mappedBy`, `cascade` e `orphanRemoval` quando adequado.
- Evitar loops em `toString()` com `@ToString(exclude = { ... })`.

## Campos obrigatórios

- `@NotBlank` para strings, `@NotNull` para objetos/enums, `@Positive` / `@PositiveOrZero` para números.
- `@Size` com limites realistas e mensagens descritivas.
- `@Enumerated(EnumType.STRING)` para enums.

## Convenções

- Nome de tabela e colunas em snake_case.
- Datas de criação com `@CreationTimestamp` e coluna `created_at`.
- Construir entidades via `builder()` para evitar construtores longos.
