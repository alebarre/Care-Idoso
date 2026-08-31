package com.careidoso.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "medicacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"idoso", "medicamento", "cuidadora"})
public class Medicacao {

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

    @NotBlank(message = "A dosagem administrada é obrigatória.")
    @Size(max = 50, message = "A dosagem administrada deve ter no máximo {max} caracteres.")
    @Column(name = "dosagem_admin", nullable = false, length = 50)
    private String dosagemAdmin;

    @NotNull(message = "O horário da medicação é obrigatório.")
    @Column(name = "horario", nullable = false)
    private LocalDateTime horario;

    @Size(max = 50, message = "A via de administração deve ter no máximo {max} caracteres.")
    @Column(length = 50)
    private String via;

    @Size(max = 1000, message = "A observação deve ter no máximo {max} caracteres.")
    @Column(length = 1000)
    private String observacao;

    @NotNull(message = "A cuidadora responsável é obrigatória.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuidadora_id", nullable = false)
    private Usuario cuidadora;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
