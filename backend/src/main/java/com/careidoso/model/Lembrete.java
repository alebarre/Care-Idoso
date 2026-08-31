package com.careidoso.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lembretes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"idoso", "medicamento"})
public class Lembrete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O idoso é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idoso_id", nullable = false)
    private Idoso idoso;

    @NotNull(message = "O medicamento é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @NotBlank(message = "O horário do lembrete é obrigatório.")
    @Size(max = 10, message = "O horário deve ter no máximo {max} caracteres.")
    @Column(nullable = false, length = 10)
    private String horario;

    @Size(max = 30, message = "Os dias da semana devem ter no máximo {max} caracteres.")
    @Column(name = "dias_semana", length = 30)
    private String diasSemana;

    @NotNull(message = "O status ativo é obrigatório.")
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Size(max = 500, message = "A observação deve ter no máximo {max} caracteres.")
    @Column(length = 500)
    private String observacao;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
