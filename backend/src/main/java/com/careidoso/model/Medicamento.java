package com.careidoso.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "medicamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do medicamento é obrigatório.")
    @Size(min = 2, max = 120, message = "O nome deve ter entre {min} e {max} caracteres.")
    @Column(nullable = false, length = 120)
    private String nome;

    @Size(max = 500, message = "A descrição deve ter no máximo {max} caracteres.")
    @Column(length = 500)
    private String descricao;

    @Size(max = 50, message = "A dosagem deve ter no máximo {max} caracteres.")
    @Column(length = 50)
    private String dosagem;

    @Size(max = 30, message = "A unidade deve ter no máximo {max} caracteres.")
    @Column(length = 30)
    private String unidade;

    @NotNull(message = "O estoque atual é obrigatório.")
    @PositiveOrZero(message = "O estoque atual não pode ser negativo.")
    @Column(name = "estoque_atual", nullable = false)
    @Builder.Default
    private Integer estoqueAtual = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
