package com.careidoso.service;

import com.careidoso.model.CodigoVerificacao;
import com.careidoso.model.TipoCodigoVerificacao;
import com.careidoso.repository.CodigoVerificacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodigoVerificacaoServiceTest {

    @Mock
    private CodigoVerificacaoRepository codigoRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CodigoVerificacaoService codigoVerificacaoService;

    @Test
    void deveRejeitarCodigoExpirado() {
        CodigoVerificacao codigoExpirado = CodigoVerificacao.builder()
                .id(1L)
                .email("teste@email.com")
                .codigo("12345")
                .tipo(TipoCodigoVerificacao.NOVA_CONTA)
                .expiracao(Instant.now().minusSeconds(1))
                .utilizado(false)
                .build();

        when(codigoRepository.findByEmailAndTipoAndCodigoAndUtilizadoFalse(
                "teste@email.com",
                TipoCodigoVerificacao.NOVA_CONTA,
                "12345"
        )).thenReturn(Optional.of(codigoExpirado));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> codigoVerificacaoService.validar("teste@email.com", "12345", TipoCodigoVerificacao.NOVA_CONTA)
        );

        assertEquals("Código expirado.", ex.getMessage());
        verify(codigoRepository, never()).save(any());
    }

    @Test
    void deveValidarCodigoDentroDoPrazo() {
        CodigoVerificacao codigoValido = CodigoVerificacao.builder()
                .id(1L)
                .email("teste@email.com")
                .codigo("12345")
                .tipo(TipoCodigoVerificacao.ESQUECI_SENHA)
                .expiracao(Instant.now().plusSeconds(60))
                .utilizado(false)
                .build();

        when(codigoRepository.findByEmailAndTipoAndCodigoAndUtilizadoFalse(
                "teste@email.com",
                TipoCodigoVerificacao.ESQUECI_SENHA,
                "12345"
        )).thenReturn(Optional.of(codigoValido));

        when(codigoRepository.save(any(CodigoVerificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        codigoVerificacaoService.validar("teste@email.com", "12345", TipoCodigoVerificacao.ESQUECI_SENHA);

        assertEquals(true, codigoValido.getUtilizado());
        verify(codigoRepository).save(codigoValido);
    }

    @Test
    void deveRejeitarCodigoInexistente() {
        when(codigoRepository.findByEmailAndTipoAndCodigoAndUtilizadoFalse(
                any(),
                any(),
                any()
        )).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> codigoVerificacaoService.validar("teste@email.com", "00000", TipoCodigoVerificacao.NOVA_CONTA)
        );

        assertEquals("Código inválido ou já utilizado.", ex.getMessage());
    }

    @Test
    void deveRejeitarCodigoComTamanhoInvalido() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> codigoVerificacaoService.validar("teste@email.com", "123", TipoCodigoVerificacao.NOVA_CONTA)
        );

        assertEquals("O código deve ter exatamente 5 dígitos.", ex.getMessage());
        verify(codigoRepository, never()).findByEmailAndTipoAndCodigoAndUtilizadoFalse(any(), any(), any());
    }
}
