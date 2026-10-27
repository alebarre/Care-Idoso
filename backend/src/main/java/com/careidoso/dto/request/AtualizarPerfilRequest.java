package com.careidoso.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Atualização parcial do próprio perfil: campos nulos são ignorados.
 * O perfil/role nunca é aceito aqui.
 */
public record AtualizarPerfilRequest(
        @Size(min = 3, max = 120, message = "O nome deve ter entre {min} e {max} caracteres.")
        String nome,

        @Email(message = "Informe um e-mail válido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo {max} caracteres.")
        String email,

        @Size(max = 100, message = "A senha atual deve ter no máximo {max} caracteres.")
        String senhaAtual,

        @Size(min = 6, max = 100, message = "A nova senha deve ter entre {min} e {max} caracteres.")
        String novaSenha
) {
}
