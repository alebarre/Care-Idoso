package com.careidoso.service;

import com.careidoso.dto.request.PedidoRequest;
import com.careidoso.model.Pedido;
import com.careidoso.model.PedidoItem;
import com.careidoso.model.Usuario;
import com.careidoso.repository.MedicamentoRepository;
import com.careidoso.repository.PedidoRepository;
import com.careidoso.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Pedidos de reposição de medicamentos/produtos feitos por cuidadoras.
 */
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final MedicamentoRepository medicamentoRepository;

    /**
     * Cria o pedido com status {@code PENDENTE} em nome da usuária autenticada (id
     * vindo do {@code SecurityContext}). Os itens são gravados em cascata.
     *
     * @throws IllegalArgumentException se algum medicamento do pedido não existir
     */
    @Transactional
    public Pedido criar(PedidoRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        var cuidadora = Usuario.builder().id(userDetails.getId()).build();

        Pedido pedido = Pedido.builder()
                .cuidadora(cuidadora)
                .status(Pedido.StatusPedido.PENDENTE)
                .observacao(request.observacao())
                .build();

        List<PedidoItem> itens = request.itens().stream()
                .map(item -> {
                    var medicamento = medicamentoRepository.findById(item.medicamentoId())
                            .orElseThrow(() -> new IllegalArgumentException("Medicamento não encontrado: " + item.medicamentoId()));
                    return PedidoItem.builder()
                            .pedido(pedido)
                            .medicamento(medicamento)
                            .quantidade(item.quantidade())
                            .observacao(item.observacao())
                            .build();
                })
                .toList();

        pedido.setItens(itens);
        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    /**
     * Pedidos da cuidadora, do mais recente para o mais antigo.
     */
    @Transactional(readOnly = true)
    public List<Pedido> listarPorCuidadora(Long cuidadoraId) {
        return pedidoRepository.findByCuidadoraIdOrderByCreatedAtDesc(cuidadoraId);
    }
}
