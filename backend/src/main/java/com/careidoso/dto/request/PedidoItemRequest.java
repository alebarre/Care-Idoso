package com.careidoso.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PedidoItemRequest(
        @NotNull(message = "O medicamento/produto é obrigatório.")
        Long medicamentoId,

        @NotNull(message = "A quantidade é obrigatória.")
        @Positive(message = "A quantidade deve ser maior que zero.")
        Integer quantidade,

        @Size(max = 500, message = "A observação deve ter no máximo {max} caracteres.")
        String observacao
) {
}
