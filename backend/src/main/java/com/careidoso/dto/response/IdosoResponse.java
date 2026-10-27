package com.careidoso.dto.response;

import java.time.LocalDate;

/**
 * Dados do idoso retornados pela API (inclui informações de saúde).
 */
public record IdosoResponse(
        Long id,
        String nome,
        LocalDate dataNascimento,
        String sexo,
        String condicoes,
        String observacoes
) {
}
