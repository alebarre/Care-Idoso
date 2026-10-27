package com.careidoso.dto.response;

/**
 * Medicamento do catálogo com seu estoque atual.
 */
public record MedicamentoResponse(
        Long id,
        String nome,
        String descricao,
        String dosagem,
        String unidade,
        Integer estoqueAtual
) {
}
