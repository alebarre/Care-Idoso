package com.careidoso.dto.request;

import com.careidoso.model.TipoCodigoVerificacao;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ValidarCodigoGenericoRequest(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo {max} caracteres.")
        String email,

        @NotBlank(message = "O código é obrigatório.")
        @Size(min = 5, max = 5, message = "O código deve ter {max} dígitos.")
        @Pattern(regexp = "^\\d{5}$", message = "O código deve conter apenas 5 dígitos numéricos.")
        String codigo,

        @NotNull(message = "O tipo de verificação é obrigatório.")
        TipoCodigoVerificacao tipo
) {
}
