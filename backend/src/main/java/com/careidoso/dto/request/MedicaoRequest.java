package com.careidoso.dto.request;

import com.careidoso.model.TipoMedicao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MedicaoRequest(
        @NotNull(message = "O idoso é obrigatório.")
        Long idosoId,

        @NotNull(message = "O tipo de medição é obrigatório.")
        TipoMedicao tipo,

        @NotBlank(message = "O valor da medição é obrigatório.")
        @Size(max = 30, message = "O valor deve ter no máximo {max} caracteres.")
        String valor,

        @Size(max = 20, message = "A unidade deve ter no máximo {max} caracteres.")
        String unidade,

        @Size(max = 1000, message = "A observação deve ter no máximo {max} caracteres.")
        String observacao
) {
}
