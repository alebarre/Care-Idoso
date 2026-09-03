package com.careidoso.controller;

import com.careidoso.dto.request.EsqueciSenhaRequest;
import com.careidoso.dto.request.LoginRequest;
import com.careidoso.dto.request.NovaContaRequest;
import com.careidoso.dto.request.RedefinirSenhaRequest;
import com.careidoso.dto.request.ValidarCodigoGenericoRequest;
import com.careidoso.dto.request.ValidarCodigoRequest;
import com.careidoso.dto.response.LoginResponse;
import com.careidoso.dto.response.MensagemResponse;
import com.careidoso.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<MensagemResponse> esqueciSenha(@RequestBody @Valid EsqueciSenhaRequest request) {
        authService.solicitarRedefinicaoSenha(request.email());
        return ResponseEntity.ok(new MensagemResponse("Se o e-mail estiver cadastrado, você receberá um código de verificação."));
    }

    @PostMapping("/validar-codigo")
    public ResponseEntity<MensagemResponse> validarCodigo(@RequestBody @Valid ValidarCodigoGenericoRequest request) {
        authService.validarCodigo(request.email(), request.codigo(), request.tipo());
        return ResponseEntity.ok(new MensagemResponse("Código verificado com sucesso."));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<MensagemResponse> redefinirSenha(@RequestBody @Valid RedefinirSenhaRequest request) {
        authService.redefinirSenha(request);
        return ResponseEntity.ok(new MensagemResponse("Senha redefinida com sucesso."));
    }

    @PostMapping("/nova-conta")
    public ResponseEntity<MensagemResponse> novaConta(@RequestBody @Valid NovaContaRequest request) {
        authService.solicitarNovaConta(request);
        return ResponseEntity.ok(new MensagemResponse("Código de verificação enviado para o e-mail informado."));
    }

    @PostMapping("/validar-codigo-nova-conta")
    public ResponseEntity<MensagemResponse> validarCodigoNovaConta(@RequestBody @Valid ValidarCodigoRequest request) {
        authService.ativarNovaConta(request.email(), request.codigo());
        return ResponseEntity.ok(new MensagemResponse("Conta ativada com sucesso. Faça login para continuar."));
    }
}
