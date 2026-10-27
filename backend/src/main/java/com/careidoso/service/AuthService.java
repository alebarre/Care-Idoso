package com.careidoso.service;

import com.careidoso.dto.request.LoginRequest;
import com.careidoso.dto.request.NovaContaRequest;
import com.careidoso.dto.request.RedefinirSenhaRequest;
import com.careidoso.dto.response.LoginResponse;
import com.careidoso.dto.response.RefreshTokenResponse;
import com.careidoso.model.Perfil;
import com.careidoso.model.TipoCodigoVerificacao;
import com.careidoso.model.Usuario;
import com.careidoso.repository.UsuarioRepository;
import com.careidoso.security.JwtService;
import com.careidoso.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras dos fluxos de autenticação: login, renovação/revogação de tokens,
 * recuperação de senha e auto-cadastro com ativação por código.
 *
 * Princípios de segurança: mensagens de erro de login não distinguem e-mail
 * inexistente de senha errada; a solicitação de recuperação de senha responde igual
 * para contas inexistentes; senhas só são gravadas com BCrypt.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CodigoVerificacaoService codigoVerificacaoService;
    private final RefreshTokenService refreshTokenService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Valida as credenciais e emite access token (JWT) + refresh token (novo registro
     * no banco). Conta inativa não autentica.
     *
     * Conta inativa só é informada a quem acertou a senha; com senha errada a resposta
     * é a mesma de qualquer credencial inválida, para não revelar quais e-mails existem.
     *
     * @throws BadCredentialsException para qualquer falha de credencial, com mensagem única
     * @throws DisabledException       se as credenciais estão corretas mas a conta está inativa
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        } catch (DisabledException e) {
            boolean senhaCorreta = usuarioRepository.findByEmail(request.email())
                    .map(u -> passwordEncoder.matches(request.senha(), u.getSenha()))
                    .orElse(false);
            if (senhaCorreta) {
                throw new DisabledException("Conta inativa. Procure o administrador.");
            }
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos."));
        UserDetailsImpl userDetails = new UserDetailsImpl(usuario);

        String accessToken = jwtService.gerarAccessToken(userDetails);
        String refreshToken = refreshTokenService.gerar(usuario);

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                userDetails.getId(),
                userDetails.getNome(),
                userDetails.getPerfil().name(),
                UsuarioService.urlFoto(usuario.getFoto())
        );
    }

    /**
     * Rotaciona o refresh token (o enviado é invalidado e um novo é emitido) e gera
     * novo access token. Reuso de token já utilizado é rejeitado.
     */
    @Transactional
    public RefreshTokenResponse refresh(String refreshTokenBruto) {
        RefreshTokenService.ResultadoRenovacao resultado = refreshTokenService.renovar(refreshTokenBruto);
        UserDetailsImpl userDetails = new UserDetailsImpl(resultado.usuario());
        String novoAccessToken = jwtService.gerarAccessToken(userDetails);

        return new RefreshTokenResponse(novoAccessToken, resultado.novoRefreshToken(), "Bearer");
    }

    /**
     * Invalida o refresh token informado; token desconhecido é ignorado (idempotente).
     */
    @Transactional
    public void logout(String refreshTokenBruto) {
        refreshTokenService.revogar(refreshTokenBruto);
    }

    /**
     * Envia código {@code ESQUECI_SENHA} se existir conta ativa para o e-mail.
     * Retorna silenciosamente quando não existe ou está inativa, para não revelar
     * quais e-mails estão cadastrados. Pode lançar erro de cooldown/envio do
     * {@code CodigoVerificacaoService}.
     */
    @Transactional
    public void solicitarRedefinicaoSenha(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())) {
            return;
        }

        codigoVerificacaoService.gerarEEnviar(email, TipoCodigoVerificacao.ESQUECI_SENHA);
    }

    /**
     * Valida o código informado. Para {@code ESQUECI_SENHA} apenas confere (não consome):
     * o código precisa ser apresentado de novo em {@link #redefinirSenha}, que o consome.
     * Os demais tipos são consumidos aqui (uso único).
     */
    @Transactional
    public void validarCodigo(String email, String codigo, TipoCodigoVerificacao tipo) {
        if (tipo == TipoCodigoVerificacao.ESQUECI_SENHA) {
            codigoVerificacaoService.verificar(email, codigo, tipo);
            return;
        }
        codigoVerificacaoService.validar(email, codigo, tipo);
    }

    /**
     * Troca a senha e revoga todos os refresh tokens do usuário (sessões abertas
     * precisam logar de novo).
     *
     * Exige e consome o código {@code ESQUECI_SENHA} do e-mail: sem um código válido a
     * senha não é alterada, mesmo que o chamador conheça o e-mail. O código é verificado
     * antes de buscar o usuário, para não revelar se o e-mail existe.
     */
    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest request) {
        codigoVerificacaoService.validar(request.email(), request.codigo(), TipoCodigoVerificacao.ESQUECI_SENHA);

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .orElseThrow(() -> new IllegalArgumentException("Código inválido ou já utilizado."));

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);
        refreshTokenService.revogarTodosDoUsuario(usuario.getId());
    }

    /**
     * Auto-cadastro: cria o usuário INATIVO (ou reaproveita/atualiza um cadastro ainda
     * inativo para o mesmo e-mail) e envia o código {@code NOVA_CONTA}. A conta só
     * passa a autenticar após {@link #ativarNovaConta}.
     *
     * Erro se já existe conta ATIVA com o e-mail. O perfil é sempre CUIDADORA e nunca
     * é alterado ao reaproveitar um cadastro inativo: o endpoint é público e aceitar o
     * perfil do cliente permitiria autoelevação a ADMIN.
     */
    @Transactional
    public void solicitarNovaConta(NovaContaRequest request) {
        usuarioRepository.findByEmail(request.email()).ifPresent(usuario -> {
            if (Boolean.TRUE.equals(usuario.getAtivo())) {
                throw new IllegalArgumentException("E-mail já cadastrado.");
            }
        });

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseGet(() -> {
                    Usuario novo = Usuario.builder()
                            .nome(request.nome())
                            .email(request.email())
                            .senha(passwordEncoder.encode(request.senha()))
                            .perfil(Perfil.CUIDADORA)
                            .ativo(false)
                            .build();
                    return usuarioRepository.save(novo);
                });

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            usuario.setNome(request.nome());
            usuario.setSenha(passwordEncoder.encode(request.senha()));
            usuarioRepository.save(usuario);
        }

        codigoVerificacaoService.gerarEEnviar(request.email(), TipoCodigoVerificacao.NOVA_CONTA);
    }

    /**
     * Consome o código {@code NOVA_CONTA} e marca a conta como ativa.
     */
    @Transactional
    public void ativarNovaConta(String email, String codigo) {
        codigoVerificacaoService.validar(email, codigo, TipoCodigoVerificacao.NOVA_CONTA);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));

        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }
}
