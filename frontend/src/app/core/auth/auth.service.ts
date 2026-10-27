import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AtualizarPerfilRequest, PerfilResponse, Usuario } from '../models/usuario.model';
import { LoginRequest, LoginResponse, RefreshTokenResponse } from '../models/login.model';
import {
  EsqueciSenhaRequest,
  MensagemResponse,
  NovaContaRequest,
  RedefinirSenhaRequest,
  ValidarCodigoGenericoRequest,
  ValidarCodigoRequest
} from '../models/esqueci-senha.model';

/** Claims do JWT lidos no cliente apenas para exibição/roteamento; a autorização real é sempre do backend. */
interface JwtPayload {
  sub: string;
  id?: number;
  nome?: string;
  perfil?: 'ADMIN' | 'CUIDADORA';
  exp?: number;
}

/**
 * Centraliza autenticação e sessão do usuário.
 *
 * Efeitos colaterais: persiste access/refresh token em `localStorage`
 * (chaves `care_idoso_token` e `care_idoso_refresh_token`) e publica o
 * usuário corrente em `usuarioLogado$`. O token JWT é decodificado só para
 * leitura de claims (sem validar assinatura); toda autorização é do backend.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = '/api/auth';
  private readonly usuariosUrl = '/api/usuarios/me';
  private readonly tokenKey = 'care_idoso_token';
  private readonly refreshTokenKey = 'care_idoso_refresh_token';

  /** Estado inicial reidratado do token salvo, para sobreviver a reload da página. */
  private usuarioLogadoSubject = new BehaviorSubject<LoginResponse | null>(this.carregarUsuario());
  usuarioLogado$ = this.usuarioLogadoSubject.asObservable();

  constructor(private http: HttpClient) { }

  /**
   * Autentica e guarda o par de tokens. Emite o usuário em `usuarioLogado$`.
   * @param email E-mail do usuário (não deve ser logado).
   * @param senha Senha em texto, enviada apenas no corpo do POST.
   */
  login(email: string, senha: string): Observable<LoginResponse> {
    const request: LoginRequest = { email, senha };
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        localStorage.setItem(this.tokenKey, response.accessToken);
        localStorage.setItem(this.refreshTokenKey, response.refreshToken);
        this.usuarioLogadoSubject.next(response);
      })
    );
  }

  /** Busca nome/foto atuais no backend (o token só guarda id, nome e perfil). */
  carregarPerfil(): Observable<Usuario> {
    return this.http.get<Usuario>(this.usuariosUrl).pipe(
      tap(usuario => this.atualizarUsuarioLogado(usuario))
    );
  }

  /**
   * Atualiza nome/e-mail/senha. O backend devolve um novo access token porque
   * o `sub` do JWT é o e-mail; sem trocá-lo, as próximas chamadas falhariam.
   */
  atualizarPerfil(request: AtualizarPerfilRequest): Observable<PerfilResponse> {
    return this.http.put<PerfilResponse>(this.usuariosUrl, request).pipe(
      tap(response => {
        localStorage.setItem(this.tokenKey, response.accessToken);
        if (response.refreshToken) {
          // trocar a senha revoga todos os refresh tokens; este mantém a sessão atual
          localStorage.setItem(this.refreshTokenKey, response.refreshToken);
        }
        this.atualizarUsuarioLogado(response.usuario);
      })
    );
  }

  /** Envia a foto (multipart, campo `arquivo`) e sincroniza o usuário logado com a resposta. */
  enviarFoto(arquivo: File): Observable<Usuario> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    return this.http.post<Usuario>(`${this.usuariosUrl}/foto`, formData).pipe(
      tap(usuario => this.atualizarUsuarioLogado(usuario))
    );
  }

  /**
   * Troca o refresh token por um novo par de tokens. O backend faz rotação:
   * o refresh token anterior é invalidado, então o novo par deve ser salvo
   * integralmente. Chamado pelo `JwtInterceptor` ao receber 401.
   */
  refreshAccessToken(): Observable<RefreshTokenResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<RefreshTokenResponse>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap(response => {
        localStorage.setItem(this.tokenKey, response.accessToken);
        localStorage.setItem(this.refreshTokenKey, response.refreshToken);
      })
    );
  }

  /**
   * Encerra a sessão: revoga o refresh token no backend (melhor esforço),
   * limpa o `localStorage` e redireciona para `/login` com recarga completa
   * da página, descartando qualquer estado em memória.
   */
  logout(): void {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      this.http.post(`${this.apiUrl}/logout`, { refreshToken }).subscribe({
        error: () => { /* logout local prossegue mesmo se a revogação falhar */ }
      });
    }

    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    this.usuarioLogadoSubject.next(null);
    window.location.href = '/login';
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshTokenKey);
  }

  /** Considera logado se houver refresh token mesmo com access expirado; o interceptor renova na próxima chamada. */
  estaLogado(): boolean {
    return !!this.getToken() || !!this.getRefreshToken();
  }

  /** Perfil do usuário (`ADMIN`/`CUIDADORA`), do estado em memória ou, na falta dele, do token. Uso apenas de UX. */
  getPerfil(): string | null {
    return this.usuarioLogadoSubject.value?.perfil ?? this.extrairPerfilDoToken();
  }

  isAdmin(): boolean {
    return this.getPerfil() === 'ADMIN';
  }

  // --- Fluxos públicos de recuperação de senha e cadastro (OTP por e-mail) ---

  esqueciSenha(request: EsqueciSenhaRequest): Observable<MensagemResponse> {
    return this.http.post<MensagemResponse>(`${this.apiUrl}/esqueci-senha`, request);
  }

  validarCodigo(request: ValidarCodigoGenericoRequest): Observable<MensagemResponse> {
    return this.http.post<MensagemResponse>(`${this.apiUrl}/validar-codigo`, request);
  }

  redefinirSenha(request: RedefinirSenhaRequest): Observable<MensagemResponse> {
    return this.http.post<MensagemResponse>(`${this.apiUrl}/redefinir-senha`, request);
  }

  novaConta(request: NovaContaRequest): Observable<MensagemResponse> {
    return this.http.post<MensagemResponse>(`${this.apiUrl}/nova-conta`, request);
  }

  validarCodigoNovaConta(request: ValidarCodigoRequest): Observable<MensagemResponse> {
    return this.http.post<MensagemResponse>(`${this.apiUrl}/validar-codigo-nova-conta`, request);
  }

  /** Mescla dados atuais do backend no usuário em memória; ignora se não houver sessão. */
  private atualizarUsuarioLogado(usuario: Usuario): void {
    const atual = this.usuarioLogadoSubject.value;
    if (!atual) {
      return;
    }
    this.usuarioLogadoSubject.next({
      ...atual,
      nome: usuario.nome,
      perfil: usuario.perfil,
      foto: usuario.foto ?? null
    });
  }

  /** Reconstrói um `LoginResponse` parcial a partir do token salvo; nome/foto reais vêm depois de `carregarPerfil()`. */
  private carregarUsuario(): LoginResponse | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }

    const payload = this.decodificarToken(token);
    return {
      accessToken: token,
      refreshToken: this.getRefreshToken() ?? '',
      tipo: 'Bearer',
      usuarioId: payload?.id ?? 0,
      nome: payload?.nome ?? '',
      perfil: payload?.perfil ?? 'CUIDADORA'
    };
  }

  private extrairPerfilDoToken(): string | null {
    const token = this.getToken();
    return this.decodificarToken(token)?.perfil ?? null;
  }

  /** Decodifica o payload (base64url) do JWT sem validar assinatura; retorna null se malformado. */
  private decodificarToken(token: string | null): JwtPayload | null {
    if (!token) {
      return null;
    }
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decoded) as JwtPayload;
    } catch {
      return null;
    }
  }
}
