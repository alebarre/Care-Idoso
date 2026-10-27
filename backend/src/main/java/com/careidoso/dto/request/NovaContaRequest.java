package com.careidoso.dto.request;

import com.careidoso.model.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Auto-cadastro público (cria conta inativa até confirmar o código por e-mail).
 *
 * @param nome   obrigatório, 3 a 120 caracteres
 * @param email  obrigatório, formato válido, até 120 caracteres
 * @param senha  obrigatória, 6 a 100 caracteres, com ao menos uma letra e um número
 * @param perfil opcional; nulo vira CUIDADORA. Como o endpoint é público, aceitar
 *               ADMIN aqui é risco de escalada de privilégio e deve ser restringido.
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
        String senha,

        Perfil perfil
) {
}
