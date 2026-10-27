package com.careidoso.service;

import com.careidoso.dto.request.AtualizarPerfilRequest;
import com.careidoso.dto.request.AtualizarUsuarioRequest;
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

/**
 * Regras de usuários: cadastro administrativo, consulta, edição do próprio perfil
 * e foto. Operações "do próprio usuário" recebem o e-mail extraído do token (nunca
 * um id vindo do cliente), o que impede alterar dados de terceiros.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FotoStorageService fotoStorageService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Rota autenticada que serve a foto do usuário logado.
     */
    public static final String URL_FOTO = "/api/usuarios/me/foto";

    /** URL da foto com o nome do arquivo como versão, para o navegador não servir cache antigo. */
    public static String urlFoto(String foto) {
        return foto == null ? null : URL_FOTO + "?v=" + foto;
    }

    /**
     * Carrega o usuário para o Spring Security (login). Usado como
     * {@code UserDetailsService} em {@code SecurityConfig}.
     *
     * @throws UsernameNotFoundException se não existir; no login o provider converte
     *         isso em credencial inválida
     */
    @Transactional(readOnly = true)
    public UserDetailsImpl carregarUserDetails(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
        return new UserDetailsImpl(usuario);
    }

    /**
     * Busca a entidade pelo e-mail (case-sensitive, como gravado).
     * Atenção: a mensagem de erro inclui o e-mail e chega ao cliente via
     * {@code GlobalExceptionHandler}; só deve ser chamada com o e-mail do próprio token.
     */
    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    /**
     * Criação administrativa de usuário (ADMIN). Senha é gravada com BCrypt; {@code ativo}
     * omitido assume {@code true}. Diferente do auto-cadastro, não exige código por e-mail.
     *
     * @throws IllegalArgumentException se o e-mail já estiver cadastrado
     */
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

    /**
     * Lista todos os usuários sem paginação (ADMIN); a senha nunca é exposta.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllByExcluidoFalseOrderByNomeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Perfil do usuário identificado pelo e-mail do token.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obter(Long id) {
        return toResponse(buscarNaoExcluido(id));
    }

    /**
     * Edita nome, e-mail e perfil de um usuário (ação de ADMIN). O ADMIN não pode alterar
     * o próprio perfil (evita ficar sem administradores). Mudar o perfil revoga os refresh
     * tokens do usuário, para o novo papel valer no próximo login.
     *
     * @throws IllegalArgumentException usuário inexistente, e-mail em uso ou tentativa de
     *         alterar o próprio perfil
     */
    @Transactional
    public UsuarioResponse atualizar(Long id, AtualizarUsuarioRequest request, String emailAdmin) {
        Usuario usuario = buscarNaoExcluido(id);

        if (ehOMesmoUsuario(usuario, emailAdmin) && request.perfil() != usuario.getPerfil()) {
            throw new IllegalArgumentException("Você não pode alterar o seu próprio perfil de acesso.");
        }
        if (usuarioRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        boolean perfilMudou = request.perfil() != usuario.getPerfil();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(request.email().trim());
        usuario.setPerfil(request.perfil());
        usuarioRepository.save(usuario);

        if (perfilMudou) {
            refreshTokenService.revogarTodosDoUsuario(usuario.getId());
        }
        log.info("Usuário {} atualizado por admin {}", usuario.getId(), LogSanitizer.mascararEmail(emailAdmin));
        return toResponse(usuario);
    }

    /**
     * Ativa ou desativa um usuário. Desativar revoga os refresh tokens; o {@code JwtFilter}
     * já recusa o access token de conta inativa. Um ADMIN não pode desativar a si mesmo.
     */
    @Transactional
    public UsuarioResponse alterarStatus(Long id, boolean ativo, String emailAdmin) {
        Usuario usuario = buscarNaoExcluido(id);

        if (!ativo && ehOMesmoUsuario(usuario, emailAdmin)) {
            throw new IllegalArgumentException("Você não pode desativar a sua própria conta.");
        }

        usuario.setAtivo(ativo);
        usuarioRepository.save(usuario);
        if (!ativo) {
            refreshTokenService.revogarTodosDoUsuario(usuario.getId());
        }
        log.info("Usuário {} {} por admin {}", usuario.getId(), ativo ? "ativado" : "desativado",
                LogSanitizer.mascararEmail(emailAdmin));
        return toResponse(usuario);
    }

    /**
     * Exclusão lógica: inativa, marca como excluído, anonimiza o e-mail (libera o original
     * para novo cadastro e impede login) e revoga os refresh tokens. O registro permanece
     * porque medicações, medições, observações e pedidos o referenciam.
     */
    @Transactional
    public void excluir(Long id, String emailAdmin) {
        Usuario usuario = buscarNaoExcluido(id);

        if (ehOMesmoUsuario(usuario, emailAdmin)) {
            throw new IllegalArgumentException("Você não pode excluir a sua própria conta.");
        }

        String foto = usuario.getFoto();
        usuario.setAtivo(false);
        usuario.setExcluido(true);
        usuario.setEmail("excluido-" + usuario.getId() + "@removido.invalid");
        usuario.setFoto(null);
        usuarioRepository.save(usuario);
        refreshTokenService.revogarTodosDoUsuario(usuario.getId());
        if (foto != null) {
            fotoStorageService.remover(foto);
        }
        log.info("Usuário {} excluído por admin {}", usuario.getId(), LogSanitizer.mascararEmail(emailAdmin));
    }

    private Usuario buscarNaoExcluido(Long id) {
        return usuarioRepository.findByIdAndExcluidoFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    private boolean ehOMesmoUsuario(Usuario usuario, String email) {
        return usuario.getEmail().equalsIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obterPerfil(String emailAutenticado) {
        return toResponse(buscarPorEmail(emailAutenticado));
    }

    /**
     * Atualização parcial do próprio perfil (nulos/vazios são ignorados).
     * O perfil/role não é alterável por aqui.
     *
     * Regras: e-mail novo não pode pertencer a outra conta; troca de senha exige a
     * senha atual correta. Devolve novo access token porque nome e e-mail fazem parte
     * das claims (o token antigo ficaria com o subject desatualizado).
     *
     * Pendências conhecidas: a unicidade do e-mail é case-sensitive (endereços que
     * diferem só em maiúsculas contam como distintos) e a troca de e-mail ou senha
     * não revoga refresh tokens existentes.
     *
     * @throws IllegalArgumentException e-mail já cadastrado ou senha atual incorreta
     */
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

    /**
     * Grava a nova foto, atualiza o usuário e só depois remove a foto anterior, para
     * não perder a foto antiga caso a gravação falhe.
     */
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

    /**
     * Nome do arquivo da foto (usado para definir o Content-Type), ou nulo.
     */
    @Transactional(readOnly = true)
    public String nomeArquivoFoto(String emailAutenticado) {
        return buscarPorEmail(emailAutenticado).getFoto();
    }

    /**
     * Converte para o DTO de saída, que nunca inclui senha.
     */
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
