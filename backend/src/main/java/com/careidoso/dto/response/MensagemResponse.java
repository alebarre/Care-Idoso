package com.careidoso.dto.response;

/**
 * Resposta simples com mensagem para o usuário.
 *
 * @param mensagem texto exibível; nunca deve conter dado sensível
 */
public record MensagemResponse(
        String mensagem
) {
}
