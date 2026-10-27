package com.careidoso.security;

import com.careidoso.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que autentica a requisição a partir do access token JWT no header
 * {@code Authorization: Bearer <token>}.
 *
 * Contrato: este filtro nunca rejeita a requisição. Token ausente, malformado,
 * com assinatura inválida ou expirado deixa o {@code SecurityContext} vazio; quem
 * devolve 401/403 é o {@code AuthenticationEntryPoint}/{@code AccessDeniedHandler}
 * configurados em {@code SecurityConfig}, conforme a regra do endpoint.
 *
 * Segurança: a autoridade ({@code ROLE_<perfil>}) vem das claims assinadas do token,
 * mas a conta precisa estar ativa no banco (uma consulta por requisição autenticada):
 * um usuário desativado perde o acesso imediatamente, sem esperar o token expirar. Já
 * uma mudança de perfil só vale no próximo token (o access token é curto).
 * Detalhes do erro vão ao log apenas como aviso, sem o token.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Extrai e valida o Bearer token e, se válido, popula o {@code SecurityContext}
     * com o e-mail (subject) como principal e o perfil como authority.
     * Qualquer falha de parse/validação é engolida (log {@code warn}) e a requisição
     * segue como anônima.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String email = jwtService.extrairEmail(token);
                String perfil = jwtService.extrairClaim(token, claims -> claims.get("perfil", String.class));

                if (email != null
                        && perfil != null
                        && SecurityContextHolder.getContext().getAuthentication() == null
                        && !jwtService.tokenExpirado(token)
                        && usuarioRepository.existsByEmailAndAtivoTrue(email)) {

                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + perfil));
                    var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    auth.setDetails(request);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                log.warn("Falha ao processar token JWT: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
