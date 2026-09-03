package com.careidoso.repository;

import com.careidoso.model.CodigoVerificacao;
import com.careidoso.model.TipoCodigoVerificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface CodigoVerificacaoRepository extends JpaRepository<CodigoVerificacao, Long> {

    Optional<CodigoVerificacao> findTopByEmailAndTipoAndUtilizadoFalseAndExpiracaoAfterOrderByCreatedAtDesc(
            String email,
            TipoCodigoVerificacao tipo,
            Instant agora
    );

    boolean existsByEmailAndTipoAndUtilizadoFalseAndCreatedAtAfter(
            String email,
            TipoCodigoVerificacao tipo,
            Instant depoisDe
    );

    @Modifying
    @Query("UPDATE CodigoVerificacao c SET c.utilizado = true WHERE c.email = :email AND c.tipo = :tipo AND c.utilizado = false")
    void invalidarCodigosPendentes(@Param("email") String email, @Param("tipo") TipoCodigoVerificacao tipo);

    Optional<CodigoVerificacao> findByEmailAndTipoAndCodigoAndUtilizadoFalse(
            String email,
            TipoCodigoVerificacao tipo,
            String codigo
    );
}
