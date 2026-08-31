package com.careidoso.service;

import com.careidoso.dto.request.ObservacaoRequest;
import com.careidoso.model.Observacao;
import com.careidoso.model.Usuario;
import com.careidoso.repository.IdosoRepository;
import com.careidoso.repository.ObservacaoRepository;
import com.careidoso.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ObservacaoService {

    private final ObservacaoRepository observacaoRepository;
    private final IdosoRepository idosoRepository;

    @Transactional
    public Observacao criar(ObservacaoRequest request) {
        var idoso = idosoRepository.findById(request.idosoId())
                .orElseThrow(() -> new IllegalArgumentException("Idoso não encontrado."));

        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        var cuidadora = Usuario.builder().id(userDetails.getId()).build();

        Observacao observacao = Observacao.builder()
                .idoso(idoso)
                .texto(request.texto())
                .cuidadora(cuidadora)
                .build();

        return observacaoRepository.save(observacao);
    }

    @Transactional(readOnly = true)
    public List<Observacao> listarPorIdoso(Long idosoId) {
        return observacaoRepository.findByIdosoIdOrderByCreatedAtDesc(idosoId);
    }
}
