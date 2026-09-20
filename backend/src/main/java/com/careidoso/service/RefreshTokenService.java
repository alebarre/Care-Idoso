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

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${careidoso.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public record ResultadoRenovacao(Usuario usuario, String novoRefreshToken) {
    }

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

    @Transactional
    public void revogar(String tokenBruto) {
        refreshTokenRepository.findByTokenHashAndUtilizadoFalse(hash(tokenBruto))
                .ifPresent(refreshToken -> {
                    refreshToken.setUtilizado(true);
                    refreshTokenRepository.save(refreshToken);
                    log.info("Refresh token revogado. usuarioId={}", refreshToken.getUsuario().getId());
                });
    }

    @Transactional
    public void revogarTodosDoUsuario(Long usuarioId) {
        refreshTokenRepository.invalidarTodosDoUsuario(usuarioId);
        log.info("Todos os refresh tokens do usuário foram revogados. usuarioId={}", usuarioId);
    }

    private String gerarTokenBruto() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

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
