package com.careidoso.repository;

import com.careidoso.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca pelo hash (nunca pelo token em claro), considerando apenas tokens ainda não usados.
     */
    Optional<RefreshToken> findByTokenHashAndUtilizadoFalse(String tokenHash);

    /**
     * Revoga em lote todos os refresh tokens ativos do usuário. Precisa de transação
     * ativa; não atualiza entidades já carregadas no contexto de persistência.
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.utilizado = true WHERE r.usuario.id = :usuarioId AND r.utilizado = false")
    void invalidarTodosDoUsuario(@Param("usuarioId") Long usuarioId);
}
