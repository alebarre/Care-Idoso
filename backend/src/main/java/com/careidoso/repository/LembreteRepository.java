package com.careidoso.repository;

import com.careidoso.model.Lembrete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LembreteRepository extends JpaRepository<Lembrete, Long> {

    List<Lembrete> findByIdosoIdAndAtivoTrueOrderByHorarioAsc(Long idosoId);

    List<Lembrete> findByAtivoTrueOrderByHorarioAsc();
}
