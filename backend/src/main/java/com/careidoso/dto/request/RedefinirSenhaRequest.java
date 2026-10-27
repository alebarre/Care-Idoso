package com.careidoso.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


/**
 * Definição de nova senha após validar o código de recuperação.
 *
 * @param email     obrigatório, formato válido, até 120 caracteres
 * @param codigo    obrigatório, exatamente 6 dígitos numéricos
 * @param novaSenha obrigatória, 6 a 100 caracteres, com ao menos uma letra e um número
 */
public record RedefinirSenhaRequest(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo {max} caracteres.")
        String email,

        @NotBlank(message = "O código é obrigatório.")
        @Size(min = 6, max = 6, message = "O código deve ter {max} dígitos.")
        @Pattern(regexp = "^\\d{6}$", message = "O código deve conter apenas 6 dígitos numéricos.")
        String codigo,

        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 6, max = 100, message = "A senha deve ter entre {min} e {max} caracteres.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A senha deve conter letras e números.")
        String novaSenha
) {
}
