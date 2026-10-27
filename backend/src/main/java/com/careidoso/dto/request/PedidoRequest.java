package com.careidoso.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Pedido de reposição. A solicitante vem do token e o status inicial é definido no servidor.
 *
 * @param itens      obrigatório, ao menos um item (cada item é validado)
 * @param observacao opcional, até 500 caracteres
 */
public record PedidoRequest(
        @NotEmpty(message = "O pedido deve conter pelo menos um item.")
        List<@Valid PedidoItemRequest> itens,

        @Size(max = 500, message = "A observação deve ter no máximo {max} caracteres.")
        String observacao
) {
}
