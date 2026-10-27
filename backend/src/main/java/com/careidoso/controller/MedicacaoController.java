package com.careidoso.controller;

import com.careidoso.dto.request.MedicacaoRequest;
import com.careidoso.model.Medicacao;
import com.careidoso.service.MedicacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Registro e histórico de medicações administradas. A cuidadora responsável é definida no servidor a partir do token. Acesso a qualquer usuário autenticado.
 */
@RestController
@RequestMapping("/api/medicacoes")
@RequiredArgsConstructor
public class MedicacaoController {

    private final MedicacaoService medicacaoService;

    @PostMapping
    public ResponseEntity<Medicacao> criar(@RequestBody @Valid MedicacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicacaoService.criar(request));
    }

    @GetMapping("/idoso/{idosoId}")
    public ResponseEntity<List<Medicacao>> listarPorIdoso(@PathVariable Long idosoId) {
        return ResponseEntity.ok(medicacaoService.listarPorIdoso(idosoId));
    }
}
