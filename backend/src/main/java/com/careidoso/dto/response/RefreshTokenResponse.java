package com.careidoso.dto.response;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        String tipo
) {
}
