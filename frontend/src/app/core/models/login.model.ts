export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tipo: string;
  usuarioId: number;
  nome: string;
  perfil: 'ADMIN' | 'CUIDADORA';
  foto?: string | null;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  tipo: string;
}
