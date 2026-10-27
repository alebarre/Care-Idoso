package com.careidoso.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Traduz exceções em respostas JSON padronizadas ({@code timestamp}, {@code mensagem}
 * e, em validação, {@code erros} por campo).
 *
 * Contrato importante: as mensagens de {@code IllegalArgumentException},
 * {@code IllegalStateException}, {@code BadCredentialsException} e
 * {@code UsernameNotFoundException} são devolvidas ao cliente como estão. Os
 * services só devem lançá-las com texto seguro para exibição (sem stack, SQL,
 * dados de terceiros ou segredos). Qualquer outra exceção cai no handler genérico,
 * que loga o detalhe no servidor e responde mensagem neutra.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Falha de Bean Validation em {@code @Valid}: 400 com o mapa campo -> mensagem.
     * A mensagem principal é a do campo {@code codigo} quando houver (fluxo de OTP, para
     * o frontend exibir direto); caso contrário, um texto genérico.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(erro -> {
            String campo = ((FieldError) erro).getField();
            String mensagem = erro.getDefaultMessage();
            erros.put(campo, mensagem);
        });

        String mensagemPrincipal = erros.getOrDefault("codigo", "Erro de validação");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpoErro(mensagemPrincipal, erros));
    }

    /**
     * 401 para credenciais ou refresh token inválidos. A mensagem é propositalmente
     * vaga (não diferencia e-mail inexistente de senha errada).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(corpoErro(ex.getMessage(), null));
    }

    /**
     * 400 para usuário não encontrado fora do fluxo de login.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corpoErro(ex.getMessage(), null));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpoErro(ex.getMessage(), null));
    }

    /**
     * Convenção do projeto: regra de negócio violada ou recurso não encontrado
     * (lançada pelos services) vira 400 com a mensagem do serviço.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpoErro(ex.getMessage(), null));
    }

    /**
     * 413 quando o upload excede o limite do multipart ({@code spring.servlet.multipart},
     * 2 MB). O limite aqui é o do container; {@code FotoStorageService} repete a checagem.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleUploadGrande(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(corpoErro("A foto excede o tamanho máximo permitido de 2 MB.", null));
    }

    /**
     * Convenção do projeto: indisponibilidade temporária (falha de e-mail, de disco ou
     * cooldown de reenvio de código) vira 503 com a mensagem do serviço.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(corpoErro(ex.getMessage(), null));
    }

    /**
     * Último recurso: registra a exceção completa só no log do servidor e responde 500
     * com mensagem neutra, sem expor stack trace nem detalhes internos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(corpoErro("Ocorreu um erro interno. Tente novamente mais tarde.", null));
    }

    /**
     * Monta o corpo padrão de erro; {@code erros} só aparece quando há detalhes por campo.
     */
    private Map<String, Object> corpoErro(String mensagem, Map<String, String> detalhes) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("mensagem", mensagem);
        if (detalhes != null) {
            corpo.put("erros", detalhes);
        }
        return corpo;
    }
}
