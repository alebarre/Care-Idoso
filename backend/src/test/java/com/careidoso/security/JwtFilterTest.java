package com.careidoso.security;

import com.careidoso.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock JwtService jwtService;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks JwtFilter jwtFilter;

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    private void executar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtFilter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    private void tokenValidoDe(String email) {
        when(jwtService.extrairEmail("token")).thenReturn(email);
        when(jwtService.extrairClaim(org.mockito.ArgumentMatchers.eq("token"), org.mockito.ArgumentMatchers.any()))
                .thenReturn("CUIDADORA");
        when(jwtService.tokenExpirado("token")).thenReturn(false);
    }

    @Test
    void usuarioAtivoComTokenValidoAutentica() throws Exception {
        tokenValidoDe("a@exemplo.com");
        when(usuarioRepository.existsByEmailAndAtivoTrue("a@exemplo.com")).thenReturn(true);

        executar();

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void usuarioDesativadoNaoAutenticaMesmoComTokenValido() throws Exception {
        tokenValidoDe("a@exemplo.com");
        when(usuarioRepository.existsByEmailAndAtivoTrue("a@exemplo.com")).thenReturn(false);

        executar();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
