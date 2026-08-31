package com.careidoso.service;

import com.careidoso.dto.request.MedicamentoRequest;
import com.careidoso.dto.response.MedicamentoResponse;
import com.careidoso.model.Medicamento;
import com.careidoso.repository.MedicamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;

    @Transactional
    public MedicamentoResponse criar(MedicamentoRequest request) {
        Medicamento medicamento = Medicamento.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .dosagem(request.dosagem())
                .unidade(request.unidade())
                .estoqueAtual(request.estoqueAtual())
                .build();

        return toResponse(medicamentoRepository.save(medicamento));
    }

    @Transactional(readOnly = true)
    public List<MedicamentoResponse> listar() {
        return medicamentoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicamentoResponse buscarPorId(Long id) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicamento não encontrado."));
        return toResponse(medicamento);
    }

    private MedicamentoResponse toResponse(Medicamento medicamento) {
        return new MedicamentoResponse(
                medicamento.getId(),
                medicamento.getNome(),
                medicamento.getDescricao(),
                medicamento.getDosagem(),
                medicamento.getUnidade(),
                medicamento.getEstoqueAtual()
        );
    }
}
