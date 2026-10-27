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

/**
 * Observações livres de cuidadoras sobre o idoso (podem conter dados de saúde: não logar o texto).
 */
@Service
@RequiredArgsConstructor
public class ObservacaoService {

    private final ObservacaoRepository observacaoRepository;
    private final IdosoRepository idosoRepository;

    /**
     * Registra a observação com a cuidadora autenticada como autora (id vindo do
     * {@code SecurityContext}).
     *
     * @throws IllegalArgumentException se o idoso não existir
     */
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

    /**
     * Observações do idoso, da mais recente para a mais antiga.
     */
    @Transactional(readOnly = true)
    public List<Observacao> listarPorIdoso(Long idosoId) {
        return observacaoRepository.findByIdosoIdOrderByCreatedAtDesc(idosoId);
    }
}
