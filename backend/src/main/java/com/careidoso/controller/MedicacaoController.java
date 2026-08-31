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
