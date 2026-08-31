package com.careidoso.repository;

import com.careidoso.model.Medicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicaoRepository extends JpaRepository<Medicao, Long> {

    List<Medicao> findByIdosoIdOrderByCreatedAtDesc(Long idosoId);
}
