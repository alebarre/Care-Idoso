package com.careidoso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Registro de observação. A autora vem do token.
 *
 * @param idosoId obrigatório; deve existir
 * @param texto   obrigatório, até 3000 caracteres
 */
public record ObservacaoRequest(
        @NotNull(message = "O idoso é obrigatório.")
        Long idosoId,

        @NotBlank(message = "O texto da observação é obrigatório.")
        @Size(max = 3000, message = "O texto deve ter no máximo {max} caracteres.")
        String texto
) {
}
