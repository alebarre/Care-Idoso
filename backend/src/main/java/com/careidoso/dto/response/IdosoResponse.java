package com.careidoso.dto.response;

import java.time.LocalDate;

public record IdosoResponse(
        Long id,
        String nome,
        LocalDate dataNascimento,
        String sexo,
        String condicoes,
        String observacoes
) {
}
