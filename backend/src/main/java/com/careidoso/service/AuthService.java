package com.careidoso.service;

import com.careidoso.dto.request.LoginRequest;
import com.careidoso.dto.request.NovaContaRequest;
import com.careidoso.dto.request.RedefinirSenhaRequest;
import com.careidoso.dto.response.LoginResponse;
import com.careidoso.model.Perfil;
import com.careidoso.model.TipoCodigoVerificacao;
import com.careidoso.model.Usuario;
import com.careidoso.repository.UsuarioRepository;
import com.careidoso.security.JwtService;
import com.careidoso.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final CodigoVerificacaoService codigoVerificacaoService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }

        UserDetailsImpl userDetails = usuarioService.carregarUserDetails(request.email());
        String token = jwtService.gerarToken(userDetails);

        return new LoginResponse(
                token,
                "Bearer",
                userDetails.getId(),
                userDetails.getNome(),
                userDetails.getPerfil().name()
        );
    }

    @Transactional
    public void solicitarRedefinicaoSenha(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())) {
            return;
        }

        codigoVerificacaoService.gerarEEnviar(email, TipoCodigoVerificacao.ESQUECI_SENHA);
    }

    @Transactional
    public void validarCodigo(String email, String codigo, TipoCodigoVerificacao tipo) {
        codigoVerificacaoService.validar(email, codigo, tipo);
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("E-mail não cadastrado."));

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void solicitarNovaConta(NovaContaRequest request) {
        usuarioRepository.findByEmail(request.email()).ifPresent(usuario -> {
            if (Boolean.TRUE.equals(usuario.getAtivo())) {
                throw new IllegalArgumentException("E-mail já cadastrado.");
            }
        });

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseGet(() -> {
                    Perfil perfil = request.perfil() != null ? request.perfil() : Perfil.CUIDADORA;
                    Usuario novo = Usuario.builder()
                            .nome(request.nome())
                            .email(request.email())
                            .senha(passwordEncoder.encode(request.senha()))
                            .perfil(perfil)
                            .ativo(false)
                            .build();
                    return usuarioRepository.save(novo);
                });

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            usuario.setNome(request.nome());
            usuario.setSenha(passwordEncoder.encode(request.senha()));
            if (request.perfil() != null) {
                usuario.setPerfil(request.perfil());
            }
            usuarioRepository.save(usuario);
        }

        codigoVerificacaoService.gerarEEnviar(request.email(), TipoCodigoVerificacao.NOVA_CONTA);
    }

    @Transactional
    public void ativarNovaConta(String email, String codigo) {
        codigoVerificacaoService.validar(email, codigo, TipoCodigoVerificacao.NOVA_CONTA);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));

        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }
}
