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

    /**
     * Código mais recente ainda utilizável (não usado e com expiração posterior a {@code agora}).
     */
    Optional<CodigoVerificacao> findTopByEmailAndTipoAndUtilizadoFalseAndExpiracaoAfterOrderByCreatedAtDesc(
            String email,
            TipoCodigoVerificacao tipo,
            Instant agora
    );

    /**
     * Há código não usado criado depois de {@code depoisDe}? Base do cooldown de reenvio.
     */
    boolean existsByEmailAndTipoAndUtilizadoFalseAndCreatedAtAfter(
            String email,
            TipoCodigoVerificacao tipo,
            Instant depoisDe
    );

    /**
     * Marca como utilizados todos os códigos pendentes do e-mail e tipo. Query de
     * atualização em lote: precisa de transação ativa (chamada a partir de service).
     */
    @Modifying
    @Query("UPDATE CodigoVerificacao c SET c.utilizado = true WHERE c.email = :email AND c.tipo = :tipo AND c.utilizado = false")
    void invalidarCodigosPendentes(@Param("email") String email, @Param("tipo") TipoCodigoVerificacao tipo);

    /**
     * Busca o código exato ainda não usado. A expiração é verificada pelo service,
     * para devolver mensagem específica de "expirado".
     */
    /** Código pendente mais recente (mesmo expirado), usado na validação com contagem de tentativas. */
    Optional<CodigoVerificacao> findTopByEmailAndTipoAndUtilizadoFalseOrderByCreatedAtDesc(
            String email,
            TipoCodigoVerificacao tipo
    );

    /** Incremento atômico, para que tentativas concorrentes não "percam" contagem. */
    @Modifying
    @Query("UPDATE CodigoVerificacao c SET c.tentativas = c.tentativas + 1 WHERE c.id = :id")
    void incrementarTentativas(@Param("id") Long id);
}
