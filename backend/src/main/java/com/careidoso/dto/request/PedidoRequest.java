package com.careidoso.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PedidoRequest(
        @NotEmpty(message = "O pedido deve conter pelo menos um item.")
        List<@Valid PedidoItemRequest> itens,

        @Size(max = 500, message = "A observação deve ter no máximo {max} caracteres.")
        String observacao
) {
}
