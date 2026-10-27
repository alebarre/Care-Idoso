package com.careidoso.service;

import com.careidoso.dto.request.NovaContaRequest;
import com.careidoso.dto.request.RedefinirSenhaRequest;
import com.careidoso.model.Perfil;
import com.careidoso.model.TipoCodigoVerificacao;
import com.careidoso.model.Usuario;
import com.careidoso.repository.UsuarioRepository;
import com.careidoso.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.careidoso.dto.request.LoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock CodigoVerificacaoService codigoVerificacaoService;
    @Mock RefreshTokenService refreshTokenService;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;

    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, jwtService, codigoVerificacaoService,
                refreshTokenService, usuarioRepository, passwordEncoder);
    }

    @Test
    void novaContaPublicaSempreCriaCuidadora() {
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(usuarioRepository.findByEmail("novo@exemplo.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        authService.solicitarNovaConta(new NovaContaRequest("Maria Silva", "novo@exemplo.com", "abc123"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, atLeastOnce()).save(captor.capture());
        captor.getAllValues().forEach(u -> assertEquals(Perfil.CUIDADORA, u.getPerfil()));
    }

    @Test
    void novaContaNaoAlteraPerfilDeCadastroInativoExistente() {
        when(passwordEncoder.encode(any())).thenReturn("hash");
        Usuario existente = Usuario.builder().nome("Admin").email("adm@exemplo.com")
                .senha("x").perfil(Perfil.ADMIN).ativo(false).cadastroConfirmado(false).build();
        when(usuarioRepository.findByEmail("adm@exemplo.com")).thenReturn(Optional.of(existente));

        authService.solicitarNovaConta(new NovaContaRequest("Outro Nome", "adm@exemplo.com", "abc123"));

        assertEquals(Perfil.ADMIN, existente.getPerfil());
    }

    @Test
    void redefinirSenhaSemCodigoValidoNaoAlteraSenha() {
        doThrow(new IllegalArgumentException("Código inválido ou já utilizado."))
                .when(codigoVerificacaoService).validar("a@exemplo.com", "123456", TipoCodigoVerificacao.ESQUECI_SENHA);

        assertThrows(IllegalArgumentException.class, () -> authService.redefinirSenha(
                new RedefinirSenhaRequest("a@exemplo.com", "123456", "novaSenha1")));

        verify(usuarioRepository, never()).save(any());
        verify(refreshTokenService, never()).revogarTodosDoUsuario(any());
    }

    @Test
    void redefinirSenhaComCodigoValidoGravaSenhaERevogaSessoes() {
        Usuario usuario = Usuario.builder().id(7L).nome("Ana").email("a@exemplo.com")
                .senha("antigo").perfil(Perfil.CUIDADORA).ativo(true).build();
        when(usuarioRepository.findByEmail("a@exemplo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha1")).thenReturn("hash-novo");

        authService.redefinirSenha(new RedefinirSenhaRequest("a@exemplo.com", "123456", "novaSenha1"));

        verify(codigoVerificacaoService).validar("a@exemplo.com", "123456", TipoCodigoVerificacao.ESQUECI_SENHA);
        assertEquals("hash-novo", usuario.getSenha());
        verify(refreshTokenService).revogarTodosDoUsuario(7L);
    }

    @Test
    void validarCodigoDeEsqueciSenhaNaoConsomeOCodigo() {
        authService.validarCodigo("a@exemplo.com", "123456", TipoCodigoVerificacao.ESQUECI_SENHA);

        verify(codigoVerificacaoService).verificar("a@exemplo.com", "123456", TipoCodigoVerificacao.ESQUECI_SENHA);
        verify(codigoVerificacaoService, never()).validar(any(), any(), any());
    }

    @Test
    void loginDeContaInativaComSenhaCorretaInformaContaInativa() {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("User is disabled"));
        Usuario inativo = Usuario.builder().email("i@exemplo.com").senha("hash").perfil(Perfil.CUIDADORA).ativo(false).build();
        when(usuarioRepository.findByEmail("i@exemplo.com")).thenReturn(Optional.of(inativo));
        when(passwordEncoder.matches("abc123", "hash")).thenReturn(true);

        DisabledException ex = assertThrows(DisabledException.class,
                () -> authService.login(new LoginRequest("i@exemplo.com", "abc123")));

        assertEquals("Conta inativa. Procure o administrador.", ex.getMessage());
    }

    @Test
    void loginDeContaInativaComSenhaErradaNaoRevelaQueAContaExiste() {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("User is disabled"));
        Usuario inativo = Usuario.builder().email("i@exemplo.com").senha("hash").perfil(Perfil.CUIDADORA).ativo(false).build();
        when(usuarioRepository.findByEmail("i@exemplo.com")).thenReturn(Optional.of(inativo));
        when(passwordEncoder.matches("errada1", "hash")).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("i@exemplo.com", "errada1")));

        assertEquals("E-mail ou senha inválidos.", ex.getMessage());
    }

    @Test
    void autoCadastroNaoReativaContaDesativadaPeloAdmin() {
        Usuario desativado = Usuario.builder().nome("Maria").email("m@exemplo.com").senha("hash-original")
                .perfil(Perfil.CUIDADORA).ativo(false).build(); // cadastroConfirmado = true (padrão)
        when(usuarioRepository.findByEmail("m@exemplo.com")).thenReturn(Optional.of(desativado));

        assertThrows(IllegalArgumentException.class,
                () -> authService.solicitarNovaConta(new NovaContaRequest("Maria Silva", "m@exemplo.com", "abc123")));

        assertEquals("hash-original", desativado.getSenha());
        verify(usuarioRepository, never()).save(any());
        verify(codigoVerificacaoService, never()).gerarEEnviar(any(), any());
    }

    @Test
    void autoCadastroReenviaParaCadastroAindaNaoConfirmado() {
        when(passwordEncoder.encode(any())).thenReturn("hash-novo");
        Usuario pendente = Usuario.builder().nome("Ana").email("a@exemplo.com").senha("antigo")
                .perfil(Perfil.CUIDADORA).ativo(false).cadastroConfirmado(false).build();
        when(usuarioRepository.findByEmail("a@exemplo.com")).thenReturn(Optional.of(pendente));

        authService.solicitarNovaConta(new NovaContaRequest("Ana Souza", "a@exemplo.com", "abc123"));

        assertEquals("hash-novo", pendente.getSenha());
        verify(codigoVerificacaoService).gerarEEnviar("a@exemplo.com", TipoCodigoVerificacao.NOVA_CONTA);
    }

    @Test
    void ativarNovaContaSoAtivaCadastroPendente() {
        Usuario desativado = Usuario.builder().email("m@exemplo.com").senha("h")
                .perfil(Perfil.CUIDADORA).ativo(false).build();
        when(usuarioRepository.findByEmail("m@exemplo.com")).thenReturn(Optional.of(desativado));

        assertThrows(IllegalArgumentException.class, () -> authService.ativarNovaConta("m@exemplo.com", "123456"));
        assertEquals(false, desativado.getAtivo());

        Usuario pendente = Usuario.builder().email("p@exemplo.com").senha("h")
                .perfil(Perfil.CUIDADORA).ativo(false).cadastroConfirmado(false).build();
        when(usuarioRepository.findByEmail("p@exemplo.com")).thenReturn(Optional.of(pendente));

        authService.ativarNovaConta("p@exemplo.com", "123456");
        assertEquals(true, pendente.getAtivo());
        assertEquals(true, pendente.getCadastroConfirmado());
    }
}
