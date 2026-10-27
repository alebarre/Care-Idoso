package com.careidoso.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Corpo de {@code /refresh} e {@code /logout}.
 *
 * @param refreshToken obrigatório; token opaco recebido no login ou na última renovação
 */
public record RefreshTokenRequest(
        @NotBlank(message = "O refresh token é obrigatório.")
        String refreshToken
) {
}
