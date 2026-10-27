/** Credenciais enviadas a `POST /api/auth/login`. */
export interface LoginRequest {
  email: string;
  senha: string;
}

/** Sessão devolvida no login: access token de curta duração e refresh token com rotação. */
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tipo: string;
  usuarioId: number;
  nome: string;
  perfil: 'ADMIN' | 'CUIDADORA';
  /** URL autenticada da foto (buscar com JWT); `null`/ausente quando não há foto. */
  foto?: string | null;
}

/** Novo par de tokens do refresh; o refresh token anterior deixa de valer. */
export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  tipo: string;
}
