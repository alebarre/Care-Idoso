package com.careidoso.repository;

import com.careidoso.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    /** Usuários não excluídos, ordenados por nome (listagem do ADMIN). */
    List<Usuario> findAllByExcluidoFalseOrderByNomeAsc();

    Optional<Usuario> findByIdAndExcluidoFalse(Long id);

    /** Usado pelo {@code JwtFilter} para recusar tokens de usuários desativados. */
    boolean existsByEmailAndAtivoTrue(String email);
}
