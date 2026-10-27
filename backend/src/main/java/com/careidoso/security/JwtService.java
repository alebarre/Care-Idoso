package com.careidoso.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Emite e valida access tokens JWT (JWS assinado com HS256).
 *
 * O token carrega, além do subject (e-mail), as claims {@code id}, {@code nome} e
 * {@code perfil}, usadas pelo {@code JwtFilter} para autorizar sem ir ao banco.
 * Não coloque dados sensíveis (senha, dados de saúde) nas claims: o payload é
 * apenas assinado, não criptografado.
 *
 * O refresh token NÃO é gerado aqui; ver {@code RefreshTokenService}.
 */
@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessExpirationMs;

    /**
     * @param secret             chave HMAC em Base64 (variável de ambiente do JWT); deve ter
     *                           pelo menos 256 bits, senão a criação da chave falha na
     *                           inicialização. Nunca versionar o valor.
     * @param accessExpirationMs validade do access token em milissegundos
     */
    public JwtService(@Value("${careidoso.jwt.secret}") String secret,
                      @Value("${careidoso.jwt.access-expiration-ms}") long accessExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpirationMs = accessExpirationMs;
    }

    /**
     * Gera um access token assinado para o usuário, com validade de
     * {@code careidoso.jwt.access-expiration-ms}.
     *
     * @param userDetails usuário já autenticado (credenciais ou refresh token válidos)
     * @return token compacto (JWS) para uso no header {@code Authorization: Bearer}
     */
    public String gerarAccessToken(UserDetailsImpl userDetails) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + accessExpirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("id", userDetails.getId())
                .claim("nome", userDetails.getNome())
                .claim("perfil", userDetails.getPerfil().name())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Indica se o token pertence ao usuário informado e não está expirado.
     * Falhas de assinatura/formato resultam em {@code false} (nunca lançam exceção).
     */
    public boolean tokenValido(String token, UserDetailsImpl userDetails) {
        try {
            String email = extrairEmail(token);
            return email.equals(userDetails.getUsername()) && !tokenExpirado(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Retorna o subject (e-mail) do token.
     *
     * @throws JwtException se a assinatura for inválida, o token malformado ou expirado
     */
    public String extrairEmail(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    public Date extrairExpiracao(String token) {
        return extrairClaim(token, Claims::getExpiration);
    }

    /**
     * Aplica {@code resolver} às claims depois de verificar a assinatura.
     * O parser do jjwt já rejeita tokens expirados, lançando {@code ExpiredJwtException}.
     */
    public <T> T extrairClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extrairTodasClaims(token);
        return resolver.apply(claims);
    }

    /**
     * Compara a expiração do token com o relógio atual. Como o parse já falha para
     * tokens expirados, na prática só retorna {@code true} em corridas de relógio;
     * mantido como checagem defensiva explícita.
     */
    public boolean tokenExpirado(String token) {
        return extrairExpiracao(token).before(new Date());
    }

    /**
     * Único ponto de parse: verifica a assinatura com a chave secreta antes de
     * devolver as claims. Nunca ler claims sem passar por aqui.
     */
    private Claims extrairTodasClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
