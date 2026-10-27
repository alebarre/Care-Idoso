package com.careidoso.dto.request;

import com.careidoso.model.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Edição de um usuário por um ADMIN ({@code PUT /api/usuarios/{id}}). Não altera senha
 * nem status; para isso existem o fluxo de redefinição e o endpoint de status.
 *
 * @param nome   obrigatório, 3 a 120 caracteres
 * @param email  obrigatório, formato válido, até 120 caracteres; deve ser único
 * @param perfil obrigatório (ADMIN ou CUIDADORA); um ADMIN não pode alterar o próprio
 */
public record AtualizarUsuarioRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 120, message = "O nome deve ter entre {min} e {max} caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo {max} caracteres.")
        String email,

        @NotNull(message = "O perfil é obrigatório.")
        Perfil perfil
) {
}
