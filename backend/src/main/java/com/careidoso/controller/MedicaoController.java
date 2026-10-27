package com.careidoso.controller;

import com.careidoso.dto.request.MedicaoRequest;
import com.careidoso.model.Medicao;
import com.careidoso.service.MedicaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Registro e histórico de medições (glicemia, pressão, temperatura). A cuidadora responsável vem do token. Acesso a qualquer usuário autenticado.
 */
@RestController
@RequestMapping("/api/medicoes")
@RequiredArgsConstructor
public class MedicaoController {

    private final MedicaoService medicaoService;

    @PostMapping
    public ResponseEntity<Medicao> criar(@RequestBody @Valid MedicaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicaoService.criar(request));
    }

    @GetMapping("/idoso/{idosoId}")
    public ResponseEntity<List<Medicao>> listarPorIdoso(@PathVariable Long idosoId) {
        return ResponseEntity.ok(medicaoService.listarPorIdoso(idosoId));
    }
}
