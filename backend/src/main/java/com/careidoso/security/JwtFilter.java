package com.careidoso.security;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

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
                        && !jwtService.tokenExpirado(token)) {

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
