package com.careidoso.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Auto-cadastro público (cria conta inativa até confirmar o código por e-mail).
 * Não aceita {@code perfil}: contas criadas aqui são sempre CUIDADORA, e um campo
 * {@code perfil} enviado pelo cliente é ignorado. ADMIN só é criado por
 * {@code POST /api/usuarios}, restrito a ADMIN.
 *
 * @param nome   obrigatório, 3 a 120 caracteres
 * @param email  obrigatório, formato válido, até 120 caracteres
 * @param senha  obrigatória, 6 a 100 caracteres, com ao menos uma letra e um número
 */
public record NovaContaRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 120, message = "O nome deve ter entre {min} e {max} caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo {max} caracteres.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, max = 100, message = "A senha deve ter entre {min} e {max} caracteres.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A senha deve conter letras e números.")
        String senha
) {
}
