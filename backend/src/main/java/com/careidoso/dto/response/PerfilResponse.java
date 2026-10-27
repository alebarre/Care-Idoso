package com.careidoso.dto.response;

/**
 * Resposta da atualização do próprio perfil. Traz um novo access token porque
 * o token carrega e-mail e nome, que podem ter mudado.
 *
 * @param refreshToken novo refresh token, presente só quando a senha foi trocada (todos os
 *                     anteriores são revogados, inclusive o da sessão atual); nulo caso contrário
 */
public record PerfilResponse(
        UsuarioResponse usuario,
        String accessToken,
        String refreshToken
) {
}
