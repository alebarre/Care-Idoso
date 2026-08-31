package com.careidoso.dto.response;

public record MedicamentoResponse(
        Long id,
        String nome,
        String descricao,
        String dosagem,
        String unidade,
        Integer estoqueAtual
) {
}
