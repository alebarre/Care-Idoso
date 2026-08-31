package com.careidoso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record MedicacaoRequest(
        @NotNull(message = "O idoso é obrigatório.")
        Long idosoId,

        @NotNull(message = "O medicamento é obrigatório.")
        Long medicamentoId,

        @NotBlank(message = "A dosagem administrada é obrigatória.")
        @Size(max = 50, message = "A dosagem administrada deve ter no máximo {max} caracteres.")
        String dosagemAdmin,

        @NotNull(message = "O horário da medicação é obrigatório.")
        LocalDateTime horario,

        @Size(max = 50, message = "A via deve ter no máximo {max} caracteres.")
        String via,

        @Size(max = 1000, message = "A observação deve ter no máximo {max} caracteres.")
        String observacao
) {
}
