package com.careidoso.dto.response;

/**
 * Resultado do login.
 *
 * @param accessToken  JWT de curta duração, enviado em {@code Authorization: Bearer}
 * @param refreshToken token opaco de uso único para obter novo par de tokens
 * @param tipo         esquema do token ({@code Bearer})
 * @param usuarioId    id do usuário
 * @param nome         nome de exibição
 * @param perfil       role ({@code ADMIN} ou {@code CUIDADORA}), para o frontend decidir a navegação (a autorização real é no backend)
 * @param foto         URL versionada da foto, ou nulo
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tipo,
        Long usuarioId,
        String nome,
        String perfil,
        String foto
) {
}
