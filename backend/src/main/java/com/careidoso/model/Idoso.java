package com.careidoso.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "idosos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Idoso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do idoso é obrigatório.")
    @Size(min = 3, max = 120, message = "O nome deve ter entre {min} e {max} caracteres.")
    @Column(nullable = false, length = 120)
    private String nome;

    @Past(message = "A data de nascimento deve ser uma data passada.")
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Size(max = 20, message = "O sexo deve ter no máximo {max} caracteres.")
    @Column(length = 20)
    private String sexo;

    @Size(max = 1000, message = "As condições devem ter no máximo {max} caracteres.")
    @Column(name = "condicoes", length = 1000)
    private String condicoes;

    @Size(max = 2000, message = "As observações devem ter no máximo {max} caracteres.")
    @Column(name = "observacoes", length = 2000)
    private String observacoes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
