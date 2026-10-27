package com.careidoso.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

    /** Relógio controlável para testar a janela sem esperar. */
    static class RelogioFalso extends Clock {
        final AtomicLong agora = new AtomicLong(1_000_000);
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(agora.get()); }
        @Override public long millis() { return agora.get(); }
    }

    private final RelogioFalso relogio = new RelogioFalso();

    private RateLimitFilter filtro(boolean confiarForwarded) {
        return new RateLimitFilter(true, 3, 60, confiarForwarded, relogio);
    }

    private MockHttpServletResponse chamar(RateLimitFilter filtro, String metodo, String caminho, String ip,
                                           String forwarded, FilterChain chain) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest(metodo, caminho);
        req.setRemoteAddr(ip);
        if (forwarded != null) {
            req.addHeader("X-Forwarded-For", forwarded);
        }
        MockHttpServletResponse res = new MockHttpServletResponse();
        filtro.doFilter(req, res, chain);
        return res;
    }

    @Test
    void bloqueiaAposOLimiteEInformaRetryAfter() throws Exception {
        RateLimitFilter filtro = filtro(false);
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 3; i++) {
            assertEquals(200, chamar(filtro, "POST", "/api/auth/login", "1.1.1.1", null, chain).getStatus());
        }
        MockHttpServletResponse bloqueada = chamar(filtro, "POST", "/api/auth/login", "1.1.1.1", null, chain);

        assertEquals(429, bloqueada.getStatus());
        assertNotNull(bloqueada.getHeader("Retry-After"));
        verify(chain, times(3)).doFilter(any(), any());
    }

    @Test
    void ipsDiferentesTemLimitesIndependentes() throws Exception {
        RateLimitFilter filtro = filtro(false);
        FilterChain chain = mock(FilterChain.class);
        for (int i = 0; i < 3; i++) {
            chamar(filtro, "POST", "/api/auth/login", "1.1.1.1", null, chain);
        }

        assertEquals(200, chamar(filtro, "POST", "/api/auth/login", "2.2.2.2", null, chain).getStatus());
    }

    @Test
    void liberaDepoisQueAJanelaPassa() throws Exception {
        RateLimitFilter filtro = filtro(false);
        FilterChain chain = mock(FilterChain.class);
        for (int i = 0; i < 3; i++) {
            chamar(filtro, "POST", "/api/auth/nova-conta", "1.1.1.1", null, chain);
        }
        assertEquals(429, chamar(filtro, "POST", "/api/auth/nova-conta", "1.1.1.1", null, chain).getStatus());

        relogio.agora.addAndGet(61_000);

        assertEquals(200, chamar(filtro, "POST", "/api/auth/nova-conta", "1.1.1.1", null, chain).getStatus());
    }

    @Test
    void ignoraMetodosECaminhosNaoLimitados() throws Exception {
        RateLimitFilter filtro = filtro(false);
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 10; i++) {
            assertEquals(200, chamar(filtro, "POST", "/api/auth/refresh", "1.1.1.1", null, chain).getStatus());
            assertEquals(200, chamar(filtro, "OPTIONS", "/api/auth/login", "1.1.1.1", null, chain).getStatus());
            assertEquals(200, chamar(filtro, "GET", "/api/usuarios/me", "1.1.1.1", null, chain).getStatus());
        }
    }

    @Test
    void forwardedForSoValeQuandoConfiavel() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        // sem confiar: forjar o header não escapa do limite (vale o IP real da conexão)
        RateLimitFilter semConfianca = filtro(false);
        for (int i = 0; i < 3; i++) {
            chamar(semConfianca, "POST", "/api/auth/login", "9.9.9.9", "10.0." + i + ".1", chain);
        }
        assertEquals(429, chamar(semConfianca, "POST", "/api/auth/login", "9.9.9.9", "10.0.9.9", chain).getStatus());

        // confiando: o IP do cliente é o primeiro do header
        RateLimitFilter comConfianca = filtro(true);
        for (int i = 0; i < 3; i++) {
            chamar(comConfianca, "POST", "/api/auth/login", "9.9.9.9", "5.5.5.5, 9.9.9.9", chain);
        }
        assertEquals(429, chamar(comConfianca, "POST", "/api/auth/login", "9.9.9.9", "5.5.5.5, 9.9.9.9", chain).getStatus());
        assertEquals(200, chamar(comConfianca, "POST", "/api/auth/login", "9.9.9.9", "6.6.6.6, 9.9.9.9", chain).getStatus());
    }
}
