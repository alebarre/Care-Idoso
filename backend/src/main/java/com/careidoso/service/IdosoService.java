package com.careidoso.service;

import com.careidoso.dto.request.IdosoRequest;
import com.careidoso.dto.response.IdosoResponse;
import com.careidoso.model.Idoso;
import com.careidoso.repository.IdosoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IdosoService {

    private final IdosoRepository idosoRepository;

    @Transactional
    public IdosoResponse criar(IdosoRequest request) {
        Idoso idoso = Idoso.builder()
                .nome(request.nome())
                .dataNascimento(request.dataNascimento())
                .sexo(request.sexo())
                .condicoes(request.condicoes())
                .observacoes(request.observacoes())
                .build();

        return toResponse(idosoRepository.save(idoso));
    }

    @Transactional(readOnly = true)
    public List<IdosoResponse> listar() {
        return idosoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IdosoResponse buscarPorId(Long id) {
        Idoso idoso = idosoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Idoso não encontrado."));
        return toResponse(idoso);
    }

    private IdosoResponse toResponse(Idoso idoso) {
        return new IdosoResponse(
                idoso.getId(),
                idoso.getNome(),
                idoso.getDataNascimento(),
                idoso.getSexo(),
                idoso.getCondicoes(),
                idoso.getObservacoes()
        );
    }
}
