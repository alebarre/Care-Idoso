package com.careidoso.service;

import com.careidoso.dto.request.AtualizarPerfilRequest;
import com.careidoso.dto.request.UsuarioRequest;
import com.careidoso.dto.response.PerfilResponse;
import com.careidoso.dto.response.UsuarioResponse;
import com.careidoso.model.Perfil;
import com.careidoso.model.Usuario;
import com.careidoso.repository.UsuarioRepository;
import com.careidoso.security.JwtService;
import com.careidoso.security.UserDetailsImpl;
import com.careidoso.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FotoStorageService fotoStorageService;

    public static final String URL_FOTO = "/api/usuarios/me/foto";

    /** URL da foto com o nome do arquivo como versão, para o navegador não servir cache antigo. */
    public static String urlFoto(String foto) {
        return foto == null ? null : URL_FOTO + "?v=" + foto;
    }

    @Transactional(readOnly = true)
    public UserDetailsImpl carregarUserDetails(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
        return new UserDetailsImpl(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .ativo(request.ativo() != null ? request.ativo() : true)
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obterPerfil(String emailAutenticado) {
        return toResponse(buscarPorEmail(emailAutenticado));
    }

    @Transactional
    public PerfilResponse atualizarPerfil(String emailAutenticado, AtualizarPerfilRequest request) {
        Usuario usuario = buscarPorEmail(emailAutenticado);

        if (request.nome() != null && !request.nome().isBlank()) {
            usuario.setNome(request.nome().trim());
        }

        if (request.email() != null && !request.email().isBlank()
                && !request.email().equalsIgnoreCase(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("E-mail já cadastrado.");
            }
            usuario.setEmail(request.email().trim());
        }

        if (request.novaSenha() != null && !request.novaSenha().isBlank()) {
            if (request.senhaAtual() == null
                    || !passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
                throw new IllegalArgumentException("A senha atual está incorreta.");
            }
            usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        }

        usuarioRepository.save(usuario);
        log.info("Perfil atualizado para usuário {}", LogSanitizer.mascararEmail(usuario.getEmail()));

        String novoToken = jwtService.gerarAccessToken(new UserDetailsImpl(usuario));
        return new PerfilResponse(toResponse(usuario), novoToken);
    }

    @Transactional
    public UsuarioResponse atualizarFoto(String emailAutenticado, MultipartFile arquivo) {
        Usuario usuario = buscarPorEmail(emailAutenticado);
        String fotoAntiga = usuario.getFoto();

        usuario.setFoto(fotoStorageService.salvar(arquivo));
        usuarioRepository.save(usuario);

        if (fotoAntiga != null) {
            fotoStorageService.remover(fotoAntiga);
        }
        log.info("Foto de perfil atualizada para usuário {}", LogSanitizer.mascararEmail(usuario.getEmail()));
        return toResponse(usuario);
    }

    /** Devolve a foto do próprio usuário, ou null se não houver. */
    @Transactional(readOnly = true)
    public Resource carregarFoto(String emailAutenticado) {
        return fotoStorageService.carregar(buscarPorEmail(emailAutenticado).getFoto());
    }

    @Transactional(readOnly = true)
    public String nomeArquivoFoto(String emailAutenticado) {
        return buscarPorEmail(emailAutenticado).getFoto();
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().name(),
                usuario.getAtivo(),
                urlFoto(usuario.getFoto())
        );
    }
}
