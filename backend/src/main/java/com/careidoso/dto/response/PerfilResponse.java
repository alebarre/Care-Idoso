package com.careidoso.dto.response;

/**
 * Resposta da atualização do próprio perfil. Traz um novo access token porque
 * o token carrega e-mail e nome, que podem ter mudado.
 */
public record PerfilResponse(
        UsuarioResponse usuario,
        String accessToken
) {
}
