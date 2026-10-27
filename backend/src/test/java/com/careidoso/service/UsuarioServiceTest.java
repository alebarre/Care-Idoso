package com.careidoso.service;

import com.careidoso.dto.request.AtualizarUsuarioRequest;
import com.careidoso.model.Perfil;
import com.careidoso.model.Usuario;
import com.careidoso.repository.UsuarioRepository;
import com.careidoso.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock FotoStorageService fotoStorageService;
    @Mock RefreshTokenService refreshTokenService;
    @InjectMocks UsuarioService usuarioService;

    Usuario admin;
    Usuario cuidadora;

    @BeforeEach
    void setUp() {
        admin = Usuario.builder().id(1L).nome("Admin").email("admin@exemplo.com").senha("h")
                .perfil(Perfil.ADMIN).ativo(true).build();
        cuidadora = Usuario.builder().id(2L).nome("Maria").email("maria@exemplo.com").senha("h")
                .perfil(Perfil.CUIDADORA).ativo(true).foto("f.jpg").build();
        lenient().when(usuarioRepository.findByIdAndExcluidoFalse(1L)).thenReturn(Optional.of(admin));
        lenient().when(usuarioRepository.findByIdAndExcluidoFalse(2L)).thenReturn(Optional.of(cuidadora));
    }

    @Test
    void adminNaoPodeDesativarASiMesmo() {
        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.alterarStatus(1L, false, "admin@exemplo.com"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void adminNaoPodeExcluirASiMesmo() {
        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.excluir(1L, "admin@exemplo.com"));
        assertFalse(admin.getExcluido());
    }

    @Test
    void adminNaoPodeAlterarOProprioPerfil() {
        var req = new AtualizarUsuarioRequest("Admin", "admin@exemplo.com", Perfil.CUIDADORA);
        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.atualizar(1L, req, "admin@exemplo.com"));
        assertEquals(Perfil.ADMIN, admin.getPerfil());
    }

    @Test
    void desativarOutroUsuarioRevogaRefreshTokens() {
        var resp = usuarioService.alterarStatus(2L, false, "admin@exemplo.com");

        assertFalse(resp.ativo());
        verify(refreshTokenService).revogarTodosDoUsuario(2L);
    }

    @Test
    void ativarOutroUsuarioNaoRevogaTokens() {
        cuidadora.setAtivo(false);
        usuarioService.alterarStatus(2L, true, "admin@exemplo.com");

        assertTrue(cuidadora.getAtivo());
        verify(refreshTokenService, never()).revogarTodosDoUsuario(any());
    }

    @Test
    void excluirFazExclusaoLogicaAnonimizaEmailERevogaSessoes() {
        usuarioService.excluir(2L, "admin@exemplo.com");

        assertTrue(cuidadora.getExcluido());
        assertFalse(cuidadora.getAtivo());
        assertEquals("excluido-2@removido.invalid", cuidadora.getEmail());
        verify(refreshTokenService).revogarTodosDoUsuario(2L);
        verify(fotoStorageService).remover("f.jpg");
    }

    @Test
    void editarComEmailDeOutroUsuarioFalha() {
        when(usuarioRepository.existsByEmailAndIdNot("admin@exemplo.com", 2L)).thenReturn(true);
        var req = new AtualizarUsuarioRequest("Maria", "admin@exemplo.com", Perfil.CUIDADORA);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.atualizar(2L, req, "admin@exemplo.com"));
    }

    @Test
    void mudarPerfilDeOutroUsuarioRevogaTokens() {
        var req = new AtualizarUsuarioRequest("Maria", "maria@exemplo.com", Perfil.ADMIN);
        usuarioService.atualizar(2L, req, "admin@exemplo.com");

        assertEquals(Perfil.ADMIN, cuidadora.getPerfil());
        verify(refreshTokenService).revogarTodosDoUsuario(2L);
    }
}
