package com.careidoso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Registro de medicação administrada. A cuidadora responsável não é enviada: vem do token.
 *
 * @param idosoId       obrigatório; deve existir
 * @param medicamentoId obrigatório; deve existir
 * @param dosagemAdmin  obrigatória, até 50 caracteres
 * @param horario       obrigatório; data/hora da administração
 * @param via           opcional, até 50 caracteres (ex.: oral)
 * @param observacao    opcional, até 1000 caracteres
 */
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
