package com.careidoso.service;

import com.careidoso.dto.request.LoginRequest;
import com.careidoso.dto.response.LoginResponse;
import com.careidoso.security.JwtService;
import com.careidoso.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }

        UserDetailsImpl userDetails = usuarioService.carregarUserDetails(request.email());
        String token = jwtService.gerarToken(userDetails);

        return new LoginResponse(
                token,
                "Bearer",
                userDetails.getId(),
                userDetails.getNome(),
                userDetails.getPerfil().name()
        );
    }
}
