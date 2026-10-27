package com.careidoso.seed;

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
 * Cria os usuários de teste (ADMIN e CUIDADORA) no startup. Ativo apenas no perfil
 * {@code dev}; as senhas são conhecidas e públicas (documentadas no projeto), então
 * este runner jamais deve rodar em produção. Idempotente: não recria usuário
 * existente.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class SeedRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        criarSeNaoExistir("Administrador", "admin@teste.com", "admin123", Perfil.ADMIN);
        criarSeNaoExistir("Cuidadora Teste", "cuidadora@teste.com", "cuidadora123", Perfil.CUIDADORA);
    }

    /**
     * Cria o usuário ativo, com senha em BCrypt, se o e-mail ainda não existir.
     */
    private void criarSeNaoExistir(String nome, String email, String senha, Perfil perfil) {
        if (usuarioRepository.existsByEmail(email)) {
            log.info("Usuário de teste já existe: {}", LogSanitizer.mascararEmail(email));
            return;
        }

        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .perfil(perfil)
                .ativo(true)
                .build();

        usuarioRepository.save(usuario);
        log.info("Usuário de teste criado: {} ({})", LogSanitizer.mascararEmail(email), perfil.name());
    }
}
