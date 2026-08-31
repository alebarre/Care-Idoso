package com.careidoso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record MedicamentoRequest(
        @NotBlank(message = "O nome do medicamento é obrigatório.")
        @Size(min = 2, max = 120, message = "O nome deve ter entre {min} e {max} caracteres.")
        String nome,

        @Size(max = 500, message = "A descrição deve ter no máximo {max} caracteres.")
        String descricao,

        @Size(max = 50, message = "A dosagem deve ter no máximo {max} caracteres.")
        String dosagem,

        @Size(max = 30, message = "A unidade deve ter no máximo {max} caracteres.")
        String unidade,

        @NotNull(message = "O estoque atual é obrigatório.")
        @PositiveOrZero(message = "O estoque atual não pode ser negativo.")
        Integer estoqueAtual
) {
}
