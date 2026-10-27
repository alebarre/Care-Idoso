package com.careidoso.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Gera (ou reaproveita) um correlationId por requisição e o mantém no MDC
 * durante todo o processamento, permitindo correlacionar logs de uma mesma
 * requisição entre front-end e back-end.
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    /**
     * Header HTTP de entrada e saída do identificador de correlação.
     */
    public static final String HEADER_NAME = "X-Correlation-Id";
    /**
     * Chave no MDC referenciada pelo padrão de log em {@code logback-spring.xml}.
     */
    public static final String MDC_KEY = "correlationId";

    /**
     * Reaproveita o header recebido ou gera um UUID, coloca no MDC, devolve no header
     * da resposta e sempre limpa o MDC no {@code finally} (threads de servlet são
     * reaproveitadas; sem a limpeza o id vazaria para outra requisição).
     *
     * Risco: o valor do header vem do cliente e vai para os logs; trate-o como dado
     * não confiável (não confiar nele para autorização ou auditoria).
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(HEADER_NAME);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER_NAME, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
