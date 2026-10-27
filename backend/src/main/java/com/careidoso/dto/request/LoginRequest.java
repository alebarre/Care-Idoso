package com.careidoso.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Credenciais de login.
 *
 * @param email obrigatório, formato válido, até 120 caracteres
 * @param senha obrigatória, 4 a 100 caracteres. O mínimo é propositalmente menor que
 *              o do cadastro (6) para não bloquear senhas legadas; a força da senha
 *              é exigida ao cadastrar ou redefinir, não ao entrar.
 */
public record LoginRequest(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo {max} caracteres.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 4, max = 100, message = "A senha deve ter entre {min} e {max} caracteres.")
        String senha
) {
}
