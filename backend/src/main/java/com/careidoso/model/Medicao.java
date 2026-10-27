package com.careidoso.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Medição de saúde (glicemia, pressão, temperatura) registrada por uma cuidadora.
 */
@Entity
@Table(name = "medicoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"idoso", "cuidadora"})
public class Medicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O idoso é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idoso_id", nullable = false)
    private Idoso idoso;

    @NotNull(message = "O tipo de medição é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMedicao tipo;

    /**
     * Valor em texto (ex.: pressão "120/80"); a interpretação depende do {@code tipo}.
     */
    @NotBlank(message = "O valor da medição é obrigatório.")
    @Size(max = 30, message = "O valor deve ter no máximo {max} caracteres.")
    @Column(nullable = false, length = 30)
    private String valor;

    @Size(max = 20, message = "A unidade deve ter no máximo {max} caracteres.")
    @Column(length = 20)
    private String unidade;

    @Size(max = 1000, message = "A observação deve ter no máximo {max} caracteres.")
    @Column(length = 1000)
    private String observacao;

    /**
     * Responsável pelo registro; definida no servidor a partir do token.
     */
    @NotNull(message = "A cuidadora responsável é obrigatória.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuidadora_id", nullable = false)
    private Usuario cuidadora;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
