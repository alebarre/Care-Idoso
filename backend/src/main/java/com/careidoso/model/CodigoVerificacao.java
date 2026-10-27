package com.careidoso.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Código numérico (OTP) enviado por e-mail para recuperar senha ou ativar conta.
 *
 * Uso único e curta duração: depois de validado ou substituído por um novo envio,
 * fica com {@code utilizado = true}. Não há FK para usuário porque o código pode
 * existir antes de a conta estar ativa; a associação é pelo e-mail. O código é
 * excluído do {@code toString} para não vazar em logs.
 */
@Entity
@Table(name = "codigos_verificacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "codigo")
public class CodigoVerificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * E-mail de destino (chave lógica com o tipo); sem FK para {@code Usuario}.
     */
    @NotBlank(message = "O e-mail é obrigatório.")
    @Column(nullable = false, length = 255)
    private String email;

    /**
     * Cinco dígitos numéricos, gravados em texto claro (vida útil de 1 minuto). Nunca logar.
     */
    @NotBlank(message = "O código é obrigatório.")
    @Size(min = 6, max = 6, message = "O código deve ter {max} dígitos.")
    @Column(nullable = false, length = 6)
    private String codigo;

    /**
     * Finalidade do código; um código de um tipo não vale para o outro.
     */
    @NotNull(message = "O tipo é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCodigoVerificacao tipo;

    /**
     * Instante limite de validade (exclusivo: a partir dele o código é expirado).
     */
    @NotNull(message = "A expiração é obrigatória.")
    @Column(nullable = false)
    private Instant expiracao;

    /**
     * Marca consumo ou invalidação; códigos utilizados nunca voltam a valer.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean utilizado = false;

    /**
     * Criação; base do cooldown de reenvio.
     */
    /** Tentativas de validação com código errado; ao atingir o limite o código deixa de ser aceito. */
    @Column(nullable = false)
    @Builder.Default
    private Integer tentativas = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
