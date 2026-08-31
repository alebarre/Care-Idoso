package com.careidoso.service;

import com.careidoso.dto.request.MedicacaoRequest;
import com.careidoso.model.Medicacao;
import com.careidoso.model.Usuario;
import com.careidoso.repository.IdosoRepository;
import com.careidoso.repository.MedicacaoRepository;
import com.careidoso.repository.MedicamentoRepository;
import com.careidoso.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicacaoService {

    private final MedicacaoRepository medicacaoRepository;
    private final IdosoRepository idosoRepository;
    private final MedicamentoRepository medicamentoRepository;

    @Transactional
    public Medicacao criar(MedicacaoRequest request) {
        var idoso = idosoRepository.findById(request.idosoId())
                .orElseThrow(() -> new IllegalArgumentException("Idoso não encontrado."));
        var medicamento = medicamentoRepository.findById(request.medicamentoId())
                .orElseThrow(() -> new IllegalArgumentException("Medicamento não encontrado."));

        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        var cuidadora = Usuario.builder().id(userDetails.getId()).build();

        Medicacao medicacao = Medicacao.builder()
                .idoso(idoso)
                .medicamento(medicamento)
                .dosagemAdmin(request.dosagemAdmin())
                .horario(request.horario())
                .via(request.via())
                .observacao(request.observacao())
                .cuidadora(cuidadora)
                .build();

        return medicacaoRepository.save(medicacao);
    }

    @Transactional(readOnly = true)
    public List<Medicacao> listarPorIdoso(Long idosoId) {
        return medicacaoRepository.findByIdosoIdOrderByHorarioDesc(idosoId);
    }
}
