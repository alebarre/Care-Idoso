package com.careidoso.controller;

import com.careidoso.dto.request.EsqueciSenhaRequest;
import com.careidoso.dto.request.LoginRequest;
import com.careidoso.dto.request.NovaContaRequest;
import com.careidoso.dto.request.RedefinirSenhaRequest;
import com.careidoso.dto.request.RefreshTokenRequest;
import com.careidoso.dto.request.ValidarCodigoGenericoRequest;
import com.careidoso.dto.request.ValidarCodigoRequest;
import com.careidoso.dto.response.LoginResponse;
import com.careidoso.dto.response.MensagemResponse;
import com.careidoso.dto.response.RefreshTokenResponse;
import com.careidoso.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints públicos de autenticação ({@code /api/auth/**}, liberados em
 * {@code SecurityConfig}). Por serem anônimos, as respostas não devem permitir
 * enumerar contas e toda a lógica fica em {@code AuthService}.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Autentica por e-mail e senha e devolve access token + refresh token.
     * Credenciais inválidas resultam em 401 genérico.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Troca um refresh token válido por novo par de tokens. O refresh token é de uso
     * único (rotação): o enviado deixa de valer. Token inválido, usado ou expirado: 401.
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    /**
     * Revoga o refresh token informado. Idempotente: token desconhecido não gera erro.
     * O access token já emitido continua válido até expirar (JWT stateless).
     */
    @PostMapping("/logout")
    public ResponseEntity<MensagemResponse> logout(@RequestBody @Valid RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(new MensagemResponse("Logout realizado com sucesso."));
    }

    /**
     * Inicia a recuperação de senha enviando código por e-mail. A resposta é sempre a
     * mesma, exista ou não a conta, para impedir enumeração de e-mails.
     */
    @PostMapping("/esqueci-senha")
    public ResponseEntity<MensagemResponse> esqueciSenha(@RequestBody @Valid EsqueciSenhaRequest request) {
        authService.solicitarRedefinicaoSenha(request.email());
        return ResponseEntity.ok(new MensagemResponse("Se o e-mail estiver cadastrado, você receberá um código de verificação."));
    }

    /**
     * Confere um código de verificação (consumindo-o) para o tipo informado.
     */
    @PostMapping("/validar-codigo")
    public ResponseEntity<MensagemResponse> validarCodigo(@RequestBody @Valid ValidarCodigoGenericoRequest request) {
        authService.validarCodigo(request.email(), request.codigo(), request.tipo());
        return ResponseEntity.ok(new MensagemResponse("Código verificado com sucesso."));
    }

    /**
     * Define nova senha para o e-mail informado. Pré-requisito: código do tipo
     * {@code ESQUECI_SENHA} previamente validado em {@code /validar-codigo}.
     * Revoga todos os refresh tokens do usuário.
     */
    @PostMapping("/redefinir-senha")
    public ResponseEntity<MensagemResponse> redefinirSenha(@RequestBody @Valid RedefinirSenhaRequest request) {
        authService.redefinirSenha(request);
        return ResponseEntity.ok(new MensagemResponse("Senha redefinida com sucesso."));
    }

    /**
     * Auto-cadastro: cria (ou atualiza) a conta inativa e envia o código de ativação.
     */
    @PostMapping("/nova-conta")
    public ResponseEntity<MensagemResponse> novaConta(@RequestBody @Valid NovaContaRequest request) {
        authService.solicitarNovaConta(request);
        return ResponseEntity.ok(new MensagemResponse("Código de verificação enviado para o e-mail informado."));
    }

    /**
     * Valida o código de ativação e ativa a conta criada em {@code /nova-conta}.
     */
    @PostMapping("/validar-codigo-nova-conta")
    public ResponseEntity<MensagemResponse> validarCodigoNovaConta(@RequestBody @Valid ValidarCodigoRequest request) {
        authService.ativarNovaConta(request.email(), request.codigo());
        return ResponseEntity.ok(new MensagemResponse("Conta ativada com sucesso. Faça login para continuar."));
    }
}
