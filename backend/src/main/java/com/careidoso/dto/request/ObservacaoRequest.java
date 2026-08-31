package com.careidoso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ObservacaoRequest(
        @NotNull(message = "O idoso é obrigatório.")
        Long idosoId,

        @NotBlank(message = "O texto da observação é obrigatório.")
        @Size(max = 3000, message = "O texto deve ter no máximo {max} caracteres.")
        String texto
) {
}
