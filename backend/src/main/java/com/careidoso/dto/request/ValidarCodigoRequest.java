package com.careidoso.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Validação do código de ativação de conta (tipo implícito {@code NOVA_CONTA}).
 *
 * @param email  obrigatório, formato válido, até 120 caracteres
 * @param codigo obrigatório, exatamente 6 dígitos numéricos
 */
public record ValidarCodigoRequest(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo {max} caracteres.")
        String email,

        @NotBlank(message = "O código é obrigatório.")
        @Size(min = 6, max = 6, message = "O código deve ter {max} dígitos.")
        @Pattern(regexp = "^\\d{6}$", message = "O código deve conter apenas 6 dígitos numéricos.")
        String codigo
) {
}
