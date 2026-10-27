package com.careidoso.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Conta de acesso ao sistema (ADMIN ou CUIDADORA). Também é referenciada como
 * cuidadora responsável nos registros de rotina.
 *
 * Contas criadas por auto-cadastro nascem inativas ({@code ativo = false}) e só
 * autenticam após a confirmação do código enviado por e-mail.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "senha")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome de exibição, de 3 a 120 caracteres.
     */
    @NotBlank(message = "O nome é obrigatório.")
    @Size(min = 3, max = 120, message = "O nome deve ter entre {min} e {max} caracteres.")
    @Column(nullable = false, length = 120)
    private String nome;

    /**
     * Identificador de login, único. Dado pessoal: mascarar com {@code LogSanitizer} ao logar.
     */
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 120, message = "O e-mail deve ter no máximo {max} caracteres.")
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /**
     * Hash BCrypt, nunca a senha em claro (daí o limite de 255 na coluna). Excluído do
     * {@code toString} e das respostas da API. A validação de 6 a 255 caracteres
     * aplica-se ao hash armazenado; as regras de senha do usuário ficam nos DTOs.
     */
    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 6, max = 255, message = "A senha deve ter entre {min} e {max} caracteres.")
    @Column(nullable = false, length = 255)
    private String senha;

    /**
     * Role de autorização; gravada como texto (enum {@code STRING}).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    /**
     * Nome do arquivo da foto de perfil (UUID + extensão) no armazenamento local; nulo
     * se não houver. Nunca contém caminho: ver {@code FotoStorageService}.
     */
    @Column(length = 100)
    private String foto;

    /**
     * Conta habilitada; {@code false} bloqueia o login (inclui contas ainda não confirmadas).
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    /**
     * Data de criação, preenchida automaticamente e imutável.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
