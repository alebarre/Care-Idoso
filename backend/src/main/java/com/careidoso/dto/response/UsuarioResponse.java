package com.careidoso.dto.response;

/**
 * Usuário exposto pela API. Nunca inclui senha nem hash.
 *
 * @param id     identificador
 * @param nome   nome de exibição
 * @param email  e-mail de login
 * @param perfil role ({@code ADMIN} ou {@code CUIDADORA})
 * @param ativo  conta habilitada
 * @param foto   URL versionada da foto (rota autenticada), ou nulo
 */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String perfil,
        Boolean ativo,
        String foto
) {
}
