package com.careidoso.service;

import com.careidoso.dto.request.LembreteRequest;
import com.careidoso.model.Lembrete;
import com.careidoso.repository.IdosoRepository;
import com.careidoso.repository.LembreteRepository;
import com.careidoso.repository.MedicamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LembreteService {

    private final LembreteRepository lembreteRepository;
    private final IdosoRepository idosoRepository;
    private final MedicamentoRepository medicamentoRepository;

    @Transactional
    public Lembrete criar(LembreteRequest request) {
        var idoso = idosoRepository.findById(request.idosoId())
                .orElseThrow(() -> new IllegalArgumentException("Idoso não encontrado."));
        var medicamento = medicamentoRepository.findById(request.medicamentoId())
                .orElseThrow(() -> new IllegalArgumentException("Medicamento não encontrado."));

        Lembrete lembrete = Lembrete.builder()
                .idoso(idoso)
                .medicamento(medicamento)
                .horario(request.horario())
                .diasSemana(request.diasSemana())
                .ativo(request.ativo() != null ? request.ativo() : true)
                .observacao(request.observacao())
                .build();

        return lembreteRepository.save(lembrete);
    }

    @Transactional(readOnly = true)
    public List<Lembrete> listar() {
        return lembreteRepository.findByAtivoTrueOrderByHorarioAsc();
    }

    @Transactional(readOnly = true)
    public List<Lembrete> listarPorIdoso(Long idosoId) {
        return lembreteRepository.findByIdosoIdAndAtivoTrueOrderByHorarioAsc(idosoId);
    }
}
