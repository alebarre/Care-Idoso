package com.careidoso.dto.request;

import com.careidoso.model.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Criação de usuário por um administrador.
 *
 * @param nome   obrigatório, 3 a 120 caracteres
 * @param email  obrigatório, formato válido, até 120 caracteres
 * @param senha  obrigatória, 6 a 100 caracteres (sem exigência de complexidade, ao contrário do auto-cadastro)
 * @param perfil obrigatório
 * @param ativo  opcional; nulo assume {@code true}
 */
public record UsuarioRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 120, message = "O nome deve ter entre {min} e {max} caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo {max} caracteres.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, max = 100, message = "A senha deve ter entre {min} e {max} caracteres.")
        String senha,

        @NotNull(message = "O perfil é obrigatório.")
        Perfil perfil,

        Boolean ativo
) {
}
