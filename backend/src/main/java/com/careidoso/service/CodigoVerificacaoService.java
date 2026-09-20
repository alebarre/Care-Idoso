package com.careidoso.service;

import com.careidoso.model.CodigoVerificacao;
import com.careidoso.model.TipoCodigoVerificacao;
import com.careidoso.repository.CodigoVerificacaoRepository;
import com.careidoso.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodigoVerificacaoService {

    private static final int TAMANHO_CODIGO = 5;
    private static final long TTL_MINUTOS = 1;
    private static final long COOLDOWN_SEGUNDOS = 60;

    private final CodigoVerificacaoRepository codigoRepository;
    private final EmailService emailService;
    private final Random random = new Random();

    @Transactional
    public void gerarEEnviar(String email, TipoCodigoVerificacao tipo) {
        Instant agora = Instant.now();
        Instant limiteCooldown = agora.minusSeconds(COOLDOWN_SEGUNDOS);

        if (codigoRepository.existsByEmailAndTipoAndUtilizadoFalseAndCreatedAtAfter(email, tipo, limiteCooldown)) {
            long segundosRestantes = tempoRestanteCooldown(email, tipo, agora);
            log.warn("Tentativa de reenvio de código dentro do cooldown. email={}, tipo={}, segundosRestantes={}",
                    LogSanitizer.mascararEmail(email), tipo, segundosRestantes);
            throw new IllegalStateException("Aguarde " + segundosRestantes + " segundos para solicitar um novo código.");
        }

        codigoRepository.invalidarCodigosPendentes(email, tipo);

        String codigo = gerarCodigo();
        CodigoVerificacao codigoVerificacao = CodigoVerificacao.builder()
                .email(email)
                .codigo(codigo)
                .tipo(tipo)
                .expiracao(agora.plus(Duration.ofMinutes(TTL_MINUTOS)))
                .utilizado(false)
                .build();

        codigoRepository.save(codigoVerificacao);
        emailService.enviarCodigoVerificacao(email, codigo, tipo);

        log.info("Código de verificação gerado. email={}, tipo={}, expiracao={}",
                LogSanitizer.mascararEmail(email), tipo, codigoVerificacao.getExpiracao());
    }

    @Transactional(readOnly = true)
    public Optional<CodigoVerificacao> buscarCodigoValido(String email, TipoCodigoVerificacao tipo) {
        return codigoRepository.findTopByEmailAndTipoAndUtilizadoFalseAndExpiracaoAfterOrderByCreatedAtDesc(
                email,
                tipo,
                Instant.now()
        );
    }

    @Transactional
    public void validar(String email, String codigoInformado, TipoCodigoVerificacao tipo) {
        Instant agora = Instant.now();
        String emailMascarado = LogSanitizer.mascararEmail(email);
        log.info("Validando código. email={}, tipo={}, agora={}", emailMascarado, tipo, agora);

        if (codigoInformado == null || codigoInformado.length() != TAMANHO_CODIGO) {
            log.warn("Código com tamanho inválido. email={}, tipo={}, tamanho={}", emailMascarado, tipo,
                    codigoInformado == null ? 0 : codigoInformado.length());
            throw new IllegalArgumentException("O código deve ter exatamente " + TAMANHO_CODIGO + " dígitos.");
        }

        CodigoVerificacao codigo = codigoRepository.findByEmailAndTipoAndCodigoAndUtilizadoFalse(email, tipo, codigoInformado)
                .orElseThrow(() -> {
                    log.warn("Código não encontrado ou já utilizado. email={}, tipo={}", emailMascarado, tipo);
                    return new IllegalArgumentException("Código inválido ou já utilizado.");
                });

        log.info("Código encontrado. id={}, expiracao={}", codigo.getId(), codigo.getExpiracao());

        if (!agora.isBefore(codigo.getExpiracao())) {
            log.warn("Código expirado. email={}, tipo={}, expiracao={}, agora={}", emailMascarado, tipo, codigo.getExpiracao(), agora);
            throw new IllegalArgumentException("Código expirado.");
        }

        codigo.setUtilizado(true);
        codigoRepository.save(codigo);
        log.info("Código validado com sucesso. email={}, tipo={}", emailMascarado, tipo);
    }

    private String gerarCodigo() {
        StringBuilder sb = new StringBuilder(TAMANHO_CODIGO);
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private long tempoRestanteCooldown(String email, TipoCodigoVerificacao tipo, Instant agora) {
        return codigoRepository.findTopByEmailAndTipoAndUtilizadoFalseAndExpiracaoAfterOrderByCreatedAtDesc(email, tipo, agora)
                .map(codigo -> {
                    long criadoEm = codigo.getCreatedAt().getEpochSecond();
                    long segundosDesdeCriacao = agora.getEpochSecond() - criadoEm;
                    return Math.max(0, COOLDOWN_SEGUNDOS - segundosDesdeCriacao);
                })
                .orElse(0L);
    }
}
