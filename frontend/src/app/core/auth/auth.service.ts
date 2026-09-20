import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, RefreshTokenResponse } from '../models/login.model';
import {
  EsqueciSenhaRequest,
  MensagemResponse,
  NovaContaRequest,
  RedefinirSenhaRequest,
  ValidarCodigoGenericoRequest,
  ValidarCodigoRequest
} from '../models/esqueci-senha.model';

interface JwtPayload {
  sub: string;
  id?: number;
  nome?: string;
  perfil?: 'ADMIN' | 'CUIDADORA';
  exp?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = '/api/auth';
  private readonly tokenKey = 'care_idoso_token';
  private readonly refreshTokenKey = 'care_idoso_refresh_token';

  private usuarioLogadoSubject = new BehaviorSubject<LoginResponse | null>(this.carregarUsuario());
  usuarioLogado$ = this.usuarioLogadoSubject.asObservable();

  constructor(private http: HttpClient) { }

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

  refreshAccessToken(): Observable<RefreshTokenResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<RefreshTokenResponse>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap(response => {
        localStorage.setItem(this.tokenKey, response.accessToken);
        localStorage.setItem(this.refreshTokenKey, response.refreshToken);
      })
    );
  }

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

  estaLogado(): boolean {
    return !!this.getToken() || !!this.getRefreshToken();
  }

  getPerfil(): string | null {
    return this.usuarioLogadoSubject.value?.perfil ?? this.extrairPerfilDoToken();
  }

  isAdmin(): boolean {
    return this.getPerfil() === 'ADMIN';
  }

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
