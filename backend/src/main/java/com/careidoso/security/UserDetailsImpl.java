package com.careidoso.security;

import com.careidoso.model.Perfil;
import com.careidoso.model.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adaptador de {@code Usuario} para o Spring Security.
 *
 * É um snapshot imutável dos dados necessários à autenticação e à emissão do JWT.
 * A authority é {@code ROLE_<perfil>} (ex.: {@code ROLE_ADMIN}), alinhada com o
 * {@code hasRole(...)} de {@code SecurityConfig}. Também é o principal usado pelos
 * services para identificar a cuidadora responsável por um registro.
 *
 * Conta com {@code ativo = false} é reportada como desabilitada
 * ({@code isEnabled}), o que bloqueia o login pelo {@code DaoAuthenticationProvider}.
 */
@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String nome;
    private final String email;
    /**
     * Hash BCrypt da senha (nunca texto plano); não deve ser logado nem serializado.
     */
    private final String senha;
    private final Perfil perfil;
    private final boolean ativo;

    public UserDetailsImpl(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.senha = usuario.getSenha();
        this.perfil = usuario.getPerfil();
        this.ativo = Boolean.TRUE.equals(usuario.getAtivo());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
