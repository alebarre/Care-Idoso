package com.careidoso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Cadastro de idoso.
 *
 * @param nome           obrigatório, 3 a 120 caracteres
 * @param dataNascimento opcional; se informada deve ser passada
 * @param sexo           opcional, até 20 caracteres
 * @param condicoes      opcional, até 1000 caracteres (dado de saúde)
 * @param observacoes    opcional, até 2000 caracteres
 */
public record IdosoRequest(
        @NotBlank(message = "O nome do idoso é obrigatório.")
        @Size(min = 3, max = 120, message = "O nome deve ter entre {min} e {max} caracteres.")
        String nome,

        @Past(message = "A data de nascimento deve ser uma data passada.")
        LocalDate dataNascimento,

        @Size(max = 20, message = "O sexo deve ter no máximo {max} caracteres.")
        String sexo,

        @Size(max = 1000, message = "As condições devem ter no máximo {max} caracteres.")
        String condicoes,

        @Size(max = 2000, message = "As observações devem ter no máximo {max} caracteres.")
        String observacoes
) {
}
