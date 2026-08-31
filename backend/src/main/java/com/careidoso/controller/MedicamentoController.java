package com.careidoso.controller;

import com.careidoso.dto.request.MedicamentoRequest;
import com.careidoso.dto.response.MedicamentoResponse;
import com.careidoso.service.MedicamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
@RequiredArgsConstructor
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    @PostMapping
    public ResponseEntity<MedicamentoResponse> criar(@RequestBody @Valid MedicamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicamentoService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<MedicamentoResponse>> listar() {
        return ResponseEntity.ok(medicamentoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicamentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicamentoService.buscarPorId(id));
    }
}
