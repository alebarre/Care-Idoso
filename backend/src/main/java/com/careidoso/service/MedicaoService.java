package com.careidoso.service;

import com.careidoso.dto.request.MedicaoRequest;
import com.careidoso.model.Medicao;
import com.careidoso.model.Usuario;
import com.careidoso.repository.IdosoRepository;
import com.careidoso.repository.MedicaoRepository;
import com.careidoso.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Registro de medições de saúde (dados sensíveis: nunca logar valores).
 */
@Service
@RequiredArgsConstructor
public class MedicaoService {

    private final MedicaoRepository medicaoRepository;
    private final IdosoRepository idosoRepository;

    /**
     * Registra a medição com a cuidadora autenticada como responsável (id vindo do
     * {@code SecurityContext}).
     *
     * @throws IllegalArgumentException se o idoso não existir
     */
    @Transactional
    public Medicao criar(MedicaoRequest request) {
        var idoso = idosoRepository.findById(request.idosoId())
                .orElseThrow(() -> new IllegalArgumentException("Idoso não encontrado."));

        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        var cuidadora = Usuario.builder().id(userDetails.getId()).build();

        Medicao medicao = Medicao.builder()
                .idoso(idoso)
                .tipo(request.tipo())
                .valor(request.valor())
                .unidade(request.unidade())
                .observacao(request.observacao())
                .cuidadora(cuidadora)
                .build();

        return medicaoRepository.save(medicao);
    }

    /**
     * Histórico do idoso, do mais recente para o mais antigo.
     */
    @Transactional(readOnly = true)
    public List<Medicao> listarPorIdoso(Long idosoId) {
        return medicaoRepository.findByIdosoIdOrderByCreatedAtDesc(idosoId);
    }
}
