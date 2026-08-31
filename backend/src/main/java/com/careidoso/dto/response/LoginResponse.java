package com.careidoso.dto.response;

public record LoginResponse(
        String token,
        String tipo,
        Long usuarioId,
        String nome,
        String perfil
) {
}
