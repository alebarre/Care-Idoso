package com.careidoso.controller;

import com.careidoso.dto.request.AtualizarPerfilRequest;
import com.careidoso.dto.request.AtualizarUsuarioRequest;
import com.careidoso.dto.request.StatusUsuarioRequest;
import com.careidoso.dto.request.UsuarioRequest;
import com.careidoso.dto.response.PerfilResponse;
import com.careidoso.dto.response.UsuarioResponse;
import com.careidoso.service.FotoStorageService;
import com.careidoso.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Gestão de usuários. Regras de acesso definidas em {@code SecurityConfig}:
 * {@code /api/usuarios/me/**} para qualquer usuário autenticado (apenas o próprio
 * perfil); demais rotas, somente ADMIN.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Cria usuário com perfil e estado definidos pelo administrador (ADMIN).
     * E-mail duplicado: 400.
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    /**
     * Lista todos os usuários (ADMIN). A resposta não inclui a senha.
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    // Gestão por ADMIN: todas as rotas /api/usuarios/** (exceto /me) exigem ADMIN em SecurityConfig.

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obter(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obter(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id,
                                                     @RequestBody @Valid AtualizarUsuarioRequest request,
                                                     Authentication authentication) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UsuarioResponse> alterarStatus(@PathVariable Long id,
                                                         @RequestBody @Valid StatusUsuarioRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.ok(usuarioService.alterarStatus(id, request.ativo(), authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, Authentication authentication) {
        usuarioService.excluir(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // Endpoints /me operam sempre sobre o usuário do token; nunca recebem id.

    /**
     * Dados do usuário autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> meuPerfil(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.obterPerfil(authentication.getName()));
    }

    /**
     * Atualização parcial do próprio perfil. Devolve novo access token porque nome e
     * e-mail (claims do token) podem ter mudado.
     */
    @PutMapping("/me")
    public ResponseEntity<PerfilResponse> atualizarMeuPerfil(Authentication authentication,
                                                             @RequestBody @Valid AtualizarPerfilRequest request) {
        return ResponseEntity.ok(usuarioService.atualizarPerfil(authentication.getName(), request));
    }

    /**
     * Upload da foto de perfil (multipart, campo {@code arquivo}, JPG/PNG até 2 MB).
     * O conteúdo é validado no servidor; ver {@code FotoStorageService}.
     */
    @PostMapping(value = "/me/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioResponse> enviarMinhaFoto(Authentication authentication,
                                                           @RequestParam("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(usuarioService.atualizarFoto(authentication.getName(), arquivo));
    }

    /**
     * Entrega a foto do próprio usuário (404 se não houver). Servida pela API, e não
     * como arquivo estático, para exigir autenticação. {@code nosniff} impede que o
     * navegador reinterprete o conteúdo e {@code private, no-cache} evita cache em
     * proxies compartilhados.
     */
    @GetMapping("/me/foto")
    public ResponseEntity<Resource> minhaFoto(Authentication authentication) {
        Resource foto = usuarioService.carregarFoto(authentication.getName());
        if (foto == null) {
            return ResponseEntity.notFound().build();
        }
        String nome = usuarioService.nomeArquivoFoto(authentication.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(FotoStorageService.tipoMidia(nome)))
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noCache().cachePrivate())
                .body(foto);
    }
}
