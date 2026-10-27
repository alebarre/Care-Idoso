package com.careidoso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Criação de lembrete de medicação.
 *
 * @param idosoId       obrigatório; deve existir
 * @param medicamentoId obrigatório; deve existir
 * @param horario       obrigatório, até 10 caracteres (formato esperado {@code HH:mm}, não validado por regex)
 * @param diasSemana    opcional, até 30 caracteres (números 1 a 7 separados por vírgula)
 * @param ativo         opcional; nulo assume {@code true}
 * @param observacao    opcional, até 500 caracteres
 */
public record LembreteRequest(
        @NotNull(message = "O idoso é obrigatório.")
        Long idosoId,

        @NotNull(message = "O medicamento é obrigatório.")
        Long medicamentoId,

        @NotBlank(message = "O horário do lembrete é obrigatório.")
        @Size(max = 10, message = "O horário deve ter no máximo {max} caracteres.")
        String horario,

        @Size(max = 30, message = "Os dias da semana devem ter no máximo {max} caracteres.")
        String diasSemana,

        Boolean ativo,

        @Size(max = 500, message = "A observação deve ter no máximo {max} caracteres.")
        String observacao
) {
}
