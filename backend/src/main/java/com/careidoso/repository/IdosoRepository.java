package com.careidoso.repository;

import com.careidoso.model.Idoso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdosoRepository extends JpaRepository<Idoso, Long> {
}
