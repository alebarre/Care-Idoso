package com.careidoso.service;

import com.careidoso.model.RefreshToken;
import com.careidoso.model.Usuario;
import com.careidoso.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Ciclo de vida dos refresh tokens (persistidos, opacos e de uso único).
 *
 * - O token entregue ao cliente é aleatório (256 bits, {@code SecureRandom}) e
 *   só o hash SHA-256 é gravado: vazamento do banco não revela tokens válidos.
 * - Rotação: cada renovação marca o token usado e emite outro; um token usado
 *   não renova de novo.
 * - Revogação é feita marcando {@code utilizado = true} (não há exclusão).
 * - Nenhum token (nem hash) é escrito em log, apenas o id do usuário.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Validade do refresh token em ms ({@code careidoso.jwt.refresh-expiration-ms}).
     */
    @Value("${careidoso.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    /**
     * Resultado de uma rotação.
     *
     * @param usuario          dono do token, usado para emitir o novo access token
     * @param novoRefreshToken token em texto claro que substitui o consumido (devolvido ao cliente uma única vez)
     */
    public record ResultadoRenovacao(Usuario usuario, String novoRefreshToken) {
    }

    /**
     * Cria e persiste um novo refresh token para o usuário.
     *
     * @return o token em texto claro; é a única oportunidade de obtê-lo, pois só o hash é guardado
     */
    @Transactional
    public String gerar(Usuario usuario) {
        String tokenBruto = gerarTokenBruto();
        Instant agora = Instant.now();

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .tokenHash(hash(tokenBruto))
                .expiracao(agora.plus(Duration.ofMillis(refreshExpirationMs)))
                .utilizado(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token gerado. usuarioId={}, expiracao={}", usuario.getId(), refreshToken.getExpiracao());

        return tokenBruto;
    }

    /**
     * Valida e consome o token (rotação) e emite outro.
     *
     * Falha com {@code BadCredentialsException} e mensagem única se o token não existe,
     * já foi utilizado/revogado ou expirou. Não há detecção de reuso (token já usado
     * apenas é recusado, sem revogar a família de tokens).
     */
    @Transactional
    public ResultadoRenovacao renovar(String tokenBruto) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndUtilizadoFalse(hash(tokenBruto))
                .orElseThrow(() -> {
                    log.warn("Tentativa de renovação com refresh token inválido ou já utilizado.");
                    return new BadCredentialsException("Refresh token inválido ou expirado.");
                });

        if (refreshToken.getExpiracao().isBefore(Instant.now())) {
            log.warn("Tentativa de renovação com refresh token expirado. usuarioId={}", refreshToken.getUsuario().getId());
            throw new BadCredentialsException("Refresh token inválido ou expirado.");
        }

        refreshToken.setUtilizado(true);
        refreshTokenRepository.save(refreshToken);

        Usuario usuario = refreshToken.getUsuario();
        String novoRefreshToken = gerar(usuario);

        log.info("Refresh token renovado. usuarioId={}", usuario.getId());

        return new ResultadoRenovacao(usuario, novoRefreshToken);
    }

    /**
     * Invalida um token específico; ignora silenciosamente tokens desconhecidos ou já usados.
     */
    @Transactional
    public void revogar(String tokenBruto) {
        refreshTokenRepository.findByTokenHashAndUtilizadoFalse(hash(tokenBruto))
                .ifPresent(refreshToken -> {
                    refreshToken.setUtilizado(true);
                    refreshTokenRepository.save(refreshToken);
                    log.info("Refresh token revogado. usuarioId={}", refreshToken.getUsuario().getId());
                });
    }

    /**
     * Invalida todos os tokens ativos do usuário (ex.: após troca de senha), encerrando
     * as sessões em outros dispositivos assim que os access tokens expirarem.
     */
    @Transactional
    public void revogarTodosDoUsuario(Long usuarioId) {
        refreshTokenRepository.invalidarTodosDoUsuario(usuarioId);
        log.info("Todos os refresh tokens do usuário foram revogados. usuarioId={}", usuarioId);
    }

    /**
     * 32 bytes aleatórios em Base64 URL-safe, sem padding.
     */
    private String gerarTokenBruto() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * SHA-256 em hexadecimal. Suficiente aqui porque o token tem entropia alta (não
     * precisa de hash lento como BCrypt) e o hash determinístico permite busca por índice.
     */
    private String hash(String tokenBruto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(tokenBruto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash indisponível.", e);
        }
    }
}
