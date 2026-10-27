package com.careidoso.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Ativação/desativação de um usuário por um ADMIN ({@code PATCH /api/usuarios/{id}/status}).
 *
 * @param ativo {@code true} libera o acesso; {@code false} bloqueia login e tokens já emitidos
 */
public record StatusUsuarioRequest(
        @NotNull(message = "O status é obrigatório.")
        Boolean ativo
) {
}
