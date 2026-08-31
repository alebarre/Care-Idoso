package com.careidoso.dto.response;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String perfil,
        Boolean ativo
) {
}
