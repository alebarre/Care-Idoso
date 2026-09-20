package com.careidoso.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tipo,
        Long usuarioId,
        String nome,
        String perfil
) {
}
