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
                .codigo("123456")
                .tipo(TipoCodigoVerificacao.NOVA_CONTA)
                .expiracao(Instant.now().minusSeconds(1))
                .utilizado(false)
                .build();

        when(codigoRepository.findTopByEmailAndTipoAndUtilizadoFalseOrderByCreatedAtDesc(
                "teste@email.com",
                TipoCodigoVerificacao.NOVA_CONTA
        )).thenReturn(Optional.of(codigoExpirado));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> codigoVerificacaoService.validar("teste@email.com", "123456", TipoCodigoVerificacao.NOVA_CONTA)
        );

        assertEquals("Código expirado.", ex.getMessage());
        verify(codigoRepository, never()).save(any());
    }

    @Test
    void deveValidarCodigoDentroDoPrazo() {
        CodigoVerificacao codigoValido = CodigoVerificacao.builder()
                .id(1L)
                .email("teste@email.com")
                .codigo("123456")
                .tipo(TipoCodigoVerificacao.ESQUECI_SENHA)
                .expiracao(Instant.now().plusSeconds(60))
                .utilizado(false)
                .build();

        when(codigoRepository.findTopByEmailAndTipoAndUtilizadoFalseOrderByCreatedAtDesc(
                "teste@email.com",
                TipoCodigoVerificacao.ESQUECI_SENHA
        )).thenReturn(Optional.of(codigoValido));

        when(codigoRepository.save(any(CodigoVerificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        codigoVerificacaoService.validar("teste@email.com", "123456", TipoCodigoVerificacao.ESQUECI_SENHA);

        assertEquals(true, codigoValido.getUtilizado());
        verify(codigoRepository).save(codigoValido);
    }

    @Test
    void deveRejeitarCodigoInexistente() {
        when(codigoRepository.findTopByEmailAndTipoAndUtilizadoFalseOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> codigoVerificacaoService.validar("teste@email.com", "000000", TipoCodigoVerificacao.NOVA_CONTA)
        );

        assertEquals("Código inválido ou já utilizado.", ex.getMessage());
    }

    @Test
    void deveRejeitarCodigoComTamanhoInvalido() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> codigoVerificacaoService.validar("teste@email.com", "123", TipoCodigoVerificacao.NOVA_CONTA)
        );

        assertEquals("O código deve ter exatamente 6 dígitos.", ex.getMessage());
        verify(codigoRepository, never()).findTopByEmailAndTipoAndUtilizadoFalseOrderByCreatedAtDesc(any(), any());
    }

    private CodigoVerificacao codigoPendente(int tentativas) {
        return CodigoVerificacao.builder()
                .id(1L)
                .email("teste@email.com")
                .codigo("123456")
                .tipo(TipoCodigoVerificacao.ESQUECI_SENHA)
                .expiracao(Instant.now().plusSeconds(60))
                .utilizado(false)
                .tentativas(tentativas)
                .build();
    }

    @Test
    void codigoErradoIncrementaTentativasENaoConsome() {
        when(codigoRepository.findTopByEmailAndTipoAndUtilizadoFalseOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.of(codigoPendente(0)));

        assertThrows(IllegalArgumentException.class,
                () -> codigoVerificacaoService.validar("teste@email.com", "654321", TipoCodigoVerificacao.ESQUECI_SENHA));

        verify(codigoRepository).incrementarTentativas(1L);
        verify(codigoRepository, never()).save(any());
    }

    @Test
    void codigoBloqueadoRejeitaAteOCodigoCorreto() {
        when(codigoRepository.findTopByEmailAndTipoAndUtilizadoFalseOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.of(codigoPendente(CodigoVerificacaoService.MAX_TENTATIVAS)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> codigoVerificacaoService.validar("teste@email.com", "123456", TipoCodigoVerificacao.ESQUECI_SENHA));

        assertEquals("Muitas tentativas. Solicite um novo código.", ex.getMessage());
        verify(codigoRepository, never()).save(any());
        verify(codigoRepository, never()).incrementarTentativas(any());
    }

    @Test
    void verificarNaoConsomeOCodigo() {
        CodigoVerificacao codigo = codigoPendente(0);
        when(codigoRepository.findTopByEmailAndTipoAndUtilizadoFalseOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.of(codigo));

        codigoVerificacaoService.verificar("teste@email.com", "123456", TipoCodigoVerificacao.ESQUECI_SENHA);

        assertEquals(false, codigo.getUtilizado());
        verify(codigoRepository, never()).save(any());
    }
}
