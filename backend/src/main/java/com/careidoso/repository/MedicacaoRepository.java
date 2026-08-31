package com.careidoso.repository;

import com.careidoso.model.Medicacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicacaoRepository extends JpaRepository<Medicacao, Long> {

    List<Medicacao> findByIdosoIdOrderByHorarioDesc(Long idosoId);
}
