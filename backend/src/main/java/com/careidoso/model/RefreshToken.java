package com.careidoso.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Refresh token persistido, opaco e de uso único (rotação a cada renovação).
 * Guarda apenas o hash SHA-256 do token, nunca o valor entregue ao cliente; o
 * hash é excluído do {@code toString}.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "tokenHash")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Dono do token. Carregado sob demanda (LAZY).
     */
    @NotNull(message = "O usuário é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * SHA-256 em hexadecimal do token; único e indexado para a busca por hash.
     */
    @NotBlank(message = "O hash do token é obrigatório.")
    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    /**
     * Validade absoluta, calculada de {@code careidoso.jwt.refresh-expiration-ms}.
     */
    @NotNull(message = "A expiração é obrigatória.")
    @Column(nullable = false)
    private Instant expiracao;

    /**
     * {@code true} depois de usado numa renovação ou revogado (logout, troca de senha).
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean utilizado = false;

    /**
     * Instante de emissão.
     */
    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;
}
