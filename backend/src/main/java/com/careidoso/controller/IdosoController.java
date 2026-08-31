package com.careidoso.controller;

import com.careidoso.dto.request.IdosoRequest;
import com.careidoso.dto.response.IdosoResponse;
import com.careidoso.service.IdosoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/idosos")
@RequiredArgsConstructor
public class IdosoController {

    private final IdosoService idosoService;

    @PostMapping
    public ResponseEntity<IdosoResponse> criar(@RequestBody @Valid IdosoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(idosoService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<IdosoResponse>> listar() {
        return ResponseEntity.ok(idosoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IdosoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(idosoService.buscarPorId(id));
    }
}
