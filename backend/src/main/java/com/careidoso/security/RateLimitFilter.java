package com.careidoso.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita por IP as requisições {@code POST} aos endpoints públicos sensíveis de
 * {@code /api/auth} (login, recuperação de senha, cadastro e validação de códigos).
 *
 * Complementa os limites por e-mail (cooldown de reenvio e tentativas por código): sem
 * isto, um atacante disparava e-mails em massa para endereços diferentes ou testava
 * senhas em muitas contas a partir de um único IP. Janela deslizante em memória; é por
 * instância, então com várias réplicas o limite efetivo é multiplicado por elas (um
 * limite compartilhado exigiria Redis ou o gateway).
 *
 * Responde {@code 429} com {@code Retry-After}. {@code /refresh} e {@code /logout} ficam
 * de fora: são chamados automaticamente pelo frontend e exigem token válido.
 *
 * O IP vem de {@code getRemoteAddr()}. {@code X-Forwarded-For} só é considerado com
 * {@code careidoso.rate-limit.trust-forwarded-for=true}, para uso atrás de um proxy
 * confiável; do contrário qualquer cliente poderia forjar o header e fugir do limite.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> CAMINHOS_LIMITADOS = Set.of(
            "/api/auth/login",
            "/api/auth/esqueci-senha",
            "/api/auth/validar-codigo",
            "/api/auth/redefinir-senha",
            "/api/auth/nova-conta",
            "/api/auth/validar-codigo-nova-conta"
    );

    private final boolean habilitado;
    private final int maxRequisicoes;
    private final long janelaMs;
    private final boolean confiarForwardedFor;
    private final Clock clock;

    /** Instantes (ms) das requisições recentes de cada IP, dentro da janela. */
    private final Map<String, Deque<Long>> requisicoesPorIp = new ConcurrentHashMap<>();
    private long ultimaLimpeza;

    @Autowired
    public RateLimitFilter(@Value("${careidoso.rate-limit.enabled:true}") boolean habilitado,
                           @Value("${careidoso.rate-limit.max-requests:20}") int maxRequisicoes,
                           @Value("${careidoso.rate-limit.window-seconds:60}") long janelaSegundos,
                           @Value("${careidoso.rate-limit.trust-forwarded-for:false}") boolean confiarForwardedFor) {
        this(habilitado, maxRequisicoes, janelaSegundos, confiarForwardedFor, Clock.systemUTC());
    }

    RateLimitFilter(boolean habilitado, int maxRequisicoes, long janelaSegundos,
                    boolean confiarForwardedFor, Clock clock) {
        this.habilitado = habilitado;
        this.maxRequisicoes = maxRequisicoes;
        this.janelaMs = janelaSegundos * 1000;
        this.confiarForwardedFor = confiarForwardedFor;
        this.clock = clock;
        this.ultimaLimpeza = clock.millis();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !habilitado
                || !"POST".equals(request.getMethod())
                || !CAMINHOS_LIMITADOS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long agora = clock.millis();
        String ip = ipDoCliente(request);
        long esperaMs = registrar(ip, agora);

        if (esperaMs > 0) {
            // o IP não é logado em claro junto de dados do usuário; apenas o caminho e a espera
            log.warn("Rate limit excedido em {}. retryAfterSegundos={}", request.getRequestURI(), (esperaMs + 999) / 1000);
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf((esperaMs + 999) / 1000));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"mensagem\":\"Muitas requisições. Aguarde alguns instantes e tente novamente.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Registra a requisição do IP. Devolve 0 se permitida, ou quantos ms faltam para
     * a requisição mais antiga sair da janela (requisição recusada não conta).
     */
    private long registrar(String ip, long agora) {
        limparSeNecessario(agora);

        Deque<Long> instantes = requisicoesPorIp.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (instantes) {
            while (!instantes.isEmpty() && agora - instantes.peekFirst() >= janelaMs) {
                instantes.pollFirst();
            }
            if (instantes.size() >= maxRequisicoes) {
                return janelaMs - (agora - instantes.peekFirst());
            }
            instantes.addLast(agora);
            return 0;
        }
    }

    /** Remove IPs sem atividade na janela, para o mapa não crescer indefinidamente. */
    private synchronized void limparSeNecessario(long agora) {
        if (agora - ultimaLimpeza < janelaMs) {
            return;
        }
        ultimaLimpeza = agora;
        requisicoesPorIp.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().isEmpty() || agora - e.getValue().peekLast() >= janelaMs;
            }
        });
    }

    private String ipDoCliente(HttpServletRequest request) {
        if (confiarForwardedFor) {
            String encaminhado = request.getHeader("X-Forwarded-For");
            if (encaminhado != null && !encaminhado.isBlank()) {
                return encaminhado.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
