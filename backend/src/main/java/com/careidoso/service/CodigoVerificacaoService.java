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

/**
 * Gera, envia e valida códigos numéricos (OTP) de verificação por e-mail,
 * usados na recuperação de senha e na ativação de conta.
 *
 * Regras: código de 5 dígitos, validade de 1 minuto, uso único, e intervalo mínimo
 * de 60 segundos entre envios para o mesmo e-mail e tipo. Um novo envio invalida os
 * códigos pendentes anteriores.
 *
 * Segurança: o código e o e-mail em claro nunca vão ao log (e-mail é mascarado).
 * Pontos de atenção: não há limite de tentativas por código nem por IP, e 5 dígitos
 * somam apenas 100 mil combinações; o cooldown e a validade curta são hoje a
 * principal proteção contra força bruta.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CodigoVerificacaoService {

    /**
     * Quantidade de dígitos do código; deve acompanhar as validações dos DTOs e a coluna do banco.
     */
    private static final int TAMANHO_CODIGO = 5;
    /**
     * Validade do código em minutos; o texto dos e-mails em {@code EmailService} cita esse valor.
     */
    private static final long TTL_MINUTOS = 1;
    /**
     * Intervalo mínimo entre envios para o mesmo e-mail e tipo.
     */
    private static final long COOLDOWN_SEGUNDOS = 60;

    private final CodigoVerificacaoRepository codigoRepository;
    private final EmailService emailService;
    /**
     * Gerador dos dígitos do OTP. {@code java.util.Random} é previsível; para um
     * código de segurança o ideal é {@code SecureRandom}.
     */
    private final Random random = new Random();

    /**
     * Gera um novo código, grava e envia por e-mail.
     *
     * Efeitos: invalida os códigos pendentes do mesmo e-mail e tipo; envia e-mail
     * (efeito externo dentro da transação: se o envio falhar, o código não é gravado).
     *
     * @throws IllegalStateException se ainda estiver no cooldown (com os segundos
     *         restantes na mensagem) ou se o envio do e-mail falhar
     */
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

    /**
     * Consulta o código vigente (não usado e não expirado) mais recente, sem consumi-lo.
     * Não use para validar entrada do usuário (use {@code validar}).
     */
    @Transactional(readOnly = true)
    public Optional<CodigoVerificacao> buscarCodigoValido(String email, TipoCodigoVerificacao tipo) {
        return codigoRepository.findTopByEmailAndTipoAndUtilizadoFalseAndExpiracaoAfterOrderByCreatedAtDesc(
                email,
                tipo,
                Instant.now()
        );
    }

    /**
     * Valida e consome (marca como utilizado) o código informado. Cada código vale
     * uma única vez; em caso de sucesso não há como reutilizá-lo.
     *
     * @throws IllegalArgumentException se o formato for inválido, o código não existir /
     *         já tiver sido usado, ou estiver expirado (mensagens exibidas ao usuário)
     */
    @Transactional
    public void validar(String email, String codigoInformado, TipoCodigoVerificacao tipo) {
        CodigoVerificacao codigo = localizarValido(email, codigoInformado, tipo);
        codigo.setUtilizado(true);
        codigoRepository.save(codigo);
        log.info("Código validado com sucesso. email={}, tipo={}", LogSanitizer.mascararEmail(email), tipo);
    }

    /**
     * Confere o código sem consumi-lo. Serve para o passo intermediário de fluxos em
     * que o código precisa ser apresentado de novo na operação final (ex.: redefinição
     * de senha), que é quem o consome via {@link #validar}.
     *
     * @throws IllegalArgumentException nas mesmas condições de {@link #validar}
     */
    @Transactional(readOnly = true)
    public void verificar(String email, String codigoInformado, TipoCodigoVerificacao tipo) {
        localizarValido(email, codigoInformado, tipo);
    }

    private CodigoVerificacao localizarValido(String email, String codigoInformado, TipoCodigoVerificacao tipo) {
        Instant agora = Instant.now();
        String emailMascarado = LogSanitizer.mascararEmail(email);

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

        if (!agora.isBefore(codigo.getExpiracao())) {
            log.warn("Código expirado. email={}, tipo={}, expiracao={}, agora={}", emailMascarado, tipo, codigo.getExpiracao(), agora);
            throw new IllegalArgumentException("Código expirado.");
        }
        return codigo;
    }

    private String gerarCodigo() {
        StringBuilder sb = new StringBuilder(TAMANHO_CODIGO);
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Segundos que faltam para liberar novo envio, para a mensagem ao usuário.
     */
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
