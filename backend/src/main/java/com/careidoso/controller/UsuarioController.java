package com.careidoso.controller;

import com.careidoso.dto.request.AtualizarPerfilRequest;
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

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    // Endpoints /me operam sempre sobre o usuário do token; nunca recebem id.

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> meuPerfil(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.obterPerfil(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<PerfilResponse> atualizarMeuPerfil(Authentication authentication,
                                                             @RequestBody @Valid AtualizarPerfilRequest request) {
        return ResponseEntity.ok(usuarioService.atualizarPerfil(authentication.getName(), request));
    }

    @PostMapping(value = "/me/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioResponse> enviarMinhaFoto(Authentication authentication,
                                                           @RequestParam("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(usuarioService.atualizarFoto(authentication.getName(), arquivo));
    }

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
