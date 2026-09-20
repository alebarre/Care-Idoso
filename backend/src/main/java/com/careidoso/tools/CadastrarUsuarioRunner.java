package com.careidoso.tools;

import com.careidoso.model.Perfil;
import com.careidoso.model.Usuario;
import com.careidoso.repository.UsuarioRepository;
import com.careidoso.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ferramenta manual para cadastrar um usuário direto no banco, com a senha já
 * criptografada em BCrypt. Não roda junto com a aplicação normal: só é ativada
 * explicitamente com o profile "cadastrar-usuario" e os dados via variáveis de ambiente.
 *
 * Exemplo de execução:
 *   NOVO_USUARIO_NOME="Maria Cuidadora" \
 *   NOVO_USUARIO_EMAIL="maria@teste.com" \
 *   NOVO_USUARIO_SENHA="senhaForte123" \
 *   NOVO_USUARIO_PERFIL="CUIDADORA" \
 *   mvn spring-boot:run -Dspring-boot.run.profiles=cadastrar-usuario
 */
@Slf4j
@Component
@Profile("cadastrar-usuario")
@RequiredArgsConstructor
public class CadastrarUsuarioRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String nome = obrigatorio("NOVO_USUARIO_NOME");
        String email = obrigatorio("NOVO_USUARIO_EMAIL");
        String senha = obrigatorio("NOVO_USUARIO_SENHA");
        String perfilTexto = System.getenv().getOrDefault("NOVO_USUARIO_PERFIL", Perfil.CUIDADORA.name());

        Perfil perfil;
        try {
            perfil = Perfil.valueOf(perfilTexto.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.error("Perfil inválido: {}. Use ADMIN ou CUIDADORA.", perfilTexto);
            return;
        }

        if (usuarioRepository.existsByEmail(email)) {
            log.warn("Já existe um usuário cadastrado com este e-mail: {}", LogSanitizer.mascararEmail(email));
            return;
        }

        String hashSenha = passwordEncoder.encode(senha);

        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(hashSenha)
                .perfil(perfil)
                .ativo(true)
                .build();

        usuarioRepository.save(usuario);

        log.info("Usuário cadastrado com sucesso: {} ({})", LogSanitizer.mascararEmail(email), perfil.name());
        log.info("Hash da senha gerado (BCrypt): {}", hashSenha);
    }

    private String obrigatorio(String variavelDeAmbiente) {
        String valor = System.getenv(variavelDeAmbiente);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Defina a variável de ambiente " + variavelDeAmbiente + " para cadastrar o usuário.");
        }
        return valor;
    }
}
