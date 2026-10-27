package com.careidoso.service;

import com.careidoso.dto.request.NovaContaRequest;
import com.careidoso.model.Perfil;
import com.careidoso.model.Usuario;
import com.careidoso.repository.UsuarioRepository;
import com.careidoso.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
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
        when(passwordEncoder.encode(any())).thenReturn("hash");
    }

    @Test
    void novaContaPublicaSempreCriaCuidadora() {
        when(usuarioRepository.findByEmail("novo@exemplo.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        authService.solicitarNovaConta(new NovaContaRequest("Maria Silva", "novo@exemplo.com", "abc123"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, atLeastOnce()).save(captor.capture());
        captor.getAllValues().forEach(u -> assertEquals(Perfil.CUIDADORA, u.getPerfil()));
    }

    @Test
    void novaContaNaoAlteraPerfilDeCadastroInativoExistente() {
        Usuario existente = Usuario.builder().nome("Admin").email("adm@exemplo.com")
                .senha("x").perfil(Perfil.ADMIN).ativo(false).build();
        when(usuarioRepository.findByEmail("adm@exemplo.com")).thenReturn(Optional.of(existente));

        authService.solicitarNovaConta(new NovaContaRequest("Outro Nome", "adm@exemplo.com", "abc123"));

        assertEquals(Perfil.ADMIN, existente.getPerfil());
    }
}
