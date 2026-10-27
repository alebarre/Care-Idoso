package com.careidoso.config;

import com.careidoso.logging.CorrelationIdFilter;
import com.careidoso.security.JwtFilter;
import com.careidoso.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração central de segurança HTTP: API stateless protegida por JWT.
 *
 * Decisões:
 * - CSRF desabilitado: não há sessão nem cookie de autenticação; o token vai no
 *   header {@code Authorization}, que o navegador não anexa automaticamente.
 * - Sessão {@code STATELESS}: nada é guardado no servidor entre requisições.
 * - A ordem dos matchers importa (vale o primeiro que casar): as regras de
 *   {@code /api/usuarios/me} vêm antes das de {@code /api/usuarios/**} para que
 *   qualquer usuário autenticado edite o próprio perfil, enquanto a gestão de
 *   usuários fica restrita a ADMIN.
 * - Autorização é sempre reforçada no backend; o frontend só esconde telas.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CorrelationIdFilter correlationIdFilter;
    private final UsuarioService usuarioService;
    private final CorsProperties corsProperties;
    private final PasswordEncoder passwordEncoder;

    /**
     * Cadeia de filtros e regras de acesso por rota.
     *
     * Rotas públicas: {@code /api/auth/**} (login, refresh, recuperação de senha e
     * cadastro) e {@code /api/publico/**}. Todo o resto exige autenticação; só a
     * gestão de usuários exige ADMIN.
     *
     * Ordem dos filtros: {@code CorrelationIdFilter} -> {@code JwtFilter} ->
     * {@code UsernamePasswordAuthenticationFilter}, de modo que todo log (inclusive
     * falhas de JWT) já carregue o correlationId.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/publico/**").permitAll()
                        .requestMatchers("/api/usuarios/me", "/api/usuarios/me/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(correlationIdFilter, JwtFilter.class)
                .build();
    }

    /**
     * Resposta 401 em JSON para requisição sem autenticação válida (token ausente,
     * inválido ou expirado). A mensagem é genérica de propósito, para não revelar o
     * motivo exato da falha.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"mensagem\":\"Não autenticado ou token expirado.\"}");
        };
    }

    /**
     * Resposta 403 em JSON para usuário autenticado sem a role exigida pela rota.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"mensagem\":\"Acesso negado.\"}");
        };
    }

    /**
     * Provider de usuário/senha usado apenas no login: carrega o usuário via
     * {@code UsuarioService} e confere a senha com BCrypt. Usuário inexistente é
     * convertido em {@code BadCredentialsException} (comportamento padrão), evitando
     * enumeração de contas; conta inativa gera {@code DisabledException}.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioService::carregarUserDetails);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Expõe o {@code AuthenticationManager} do Spring para o {@code AuthService.login}.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS restrito às origens configuradas em {@code CorsProperties}. Com
     * {@code allowCredentials = true} o curinga {@code *} em origens é proibido pelo
     * Spring, e não deve ser contornado.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(corsProperties.getAllowedOrigins().split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
