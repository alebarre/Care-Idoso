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

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final MedicamentoRepository medicamentoRepository;

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

    @Transactional(readOnly = true)
    public List<Pedido> listarPorCuidadora(Long cuidadoraId) {
        return pedidoRepository.findByCuidadoraIdOrderByCreatedAtDesc(cuidadoraId);
    }
}
