package com.careidoso.repository;

import com.careidoso.model.Observacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservacaoRepository extends JpaRepository<Observacao, Long> {

    List<Observacao> findByIdosoIdOrderByCreatedAtDesc(Long idosoId);
}
