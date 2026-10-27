package com.careidoso.dto.response;

/**
 * Novo par de tokens após uma renovação.
 *
 * @param accessToken  novo JWT
 * @param refreshToken novo refresh token; o anterior foi invalidado e deve ser descartado
 * @param tipo         esquema do token ({@code Bearer})
 */
public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        String tipo
) {
}
