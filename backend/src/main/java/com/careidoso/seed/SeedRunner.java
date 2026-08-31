package com.careidoso.seed;

import com.careidoso.model.Perfil;
import com.careidoso.model.Usuario;
import com.careidoso.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

    private void criarSeNaoExistir(String nome, String email, String senha, Perfil perfil) {
        if (usuarioRepository.existsByEmail(email)) {
            log.info("Usuário de teste já existe: {}", email);
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
        log.info("Usuário de teste criado: {} ({})", email, perfil.name());
    }
}
