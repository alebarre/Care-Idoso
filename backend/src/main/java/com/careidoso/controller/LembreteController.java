package com.careidoso.controller;

import com.careidoso.dto.request.LembreteRequest;
import com.careidoso.model.Lembrete;
import com.careidoso.service.LembreteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lembretes")
@RequiredArgsConstructor
public class LembreteController {

    private final LembreteService lembreteService;

    @PostMapping
    public ResponseEntity<Lembrete> criar(@RequestBody @Valid LembreteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lembreteService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<Lembrete>> listar() {
        return ResponseEntity.ok(lembreteService.listar());
    }

    @GetMapping("/idoso/{idosoId}")
    public ResponseEntity<List<Lembrete>> listarPorIdoso(@PathVariable Long idosoId) {
        return ResponseEntity.ok(lembreteService.listarPorIdoso(idosoId));
    }
}
