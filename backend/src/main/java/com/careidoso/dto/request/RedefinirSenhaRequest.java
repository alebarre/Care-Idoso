package com.careidoso.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record RedefinirSenhaRequest(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo {max} caracteres.")
        String email,

        @NotBlank(message = "O código é obrigatório.")
        @Size(min = 5, max = 5, message = "O código deve ter {max} dígitos.")
        @Pattern(regexp = "^\\d{5}$", message = "O código deve conter apenas 5 dígitos numéricos.")
        String codigo,

        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 6, max = 100, message = "A senha deve ter entre {min} e {max} caracteres.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A senha deve conter letras e números.")
        String novaSenha
) {
}
