package com.careidoso.controller;

import com.careidoso.dto.request.ObservacaoRequest;
import com.careidoso.model.Observacao;
import com.careidoso.service.ObservacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/observacoes")
@RequiredArgsConstructor
public class ObservacaoController {

    private final ObservacaoService observacaoService;

    @PostMapping
    public ResponseEntity<Observacao> criar(@RequestBody @Valid ObservacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(observacaoService.criar(request));
    }

    @GetMapping("/idoso/{idosoId}")
    public ResponseEntity<List<Observacao>> listarPorIdoso(@PathVariable Long idosoId) {
        return ResponseEntity.ok(observacaoService.listarPorIdoso(idosoId));
    }
}
