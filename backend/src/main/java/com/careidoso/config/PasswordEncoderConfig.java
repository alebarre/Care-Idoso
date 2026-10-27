package com.careidoso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Declara o codificador de senhas único da aplicação (BCrypt).
 * Isolado em classe própria para evitar dependência circular com {@code SecurityConfig},
 * que depende de {@code UsuarioService}, que por sua vez precisa do encoder.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * BCrypt com força padrão. Todo armazenamento de senha deve passar por este bean.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
