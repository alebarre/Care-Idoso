package com.careidoso.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

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

    @NotBlank(message = "O e-mail é obrigatório.")
    @Column(nullable = false, length = 255)
    private String email;

    @NotBlank(message = "O código é obrigatório.")
    @Size(min = 5, max = 5, message = "O código deve ter {max} dígitos.")
    @Column(nullable = false, length = 5)
    private String codigo;

    @NotNull(message = "O tipo é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCodigoVerificacao tipo;

    @NotNull(message = "A expiração é obrigatória.")
    @Column(nullable = false)
    private Instant expiracao;

    @Column(nullable = false)
    @Builder.Default
    private Boolean utilizado = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
