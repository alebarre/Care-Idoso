export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  usuarioId: number;
  nome: string;
  perfil: 'ADMIN' | 'CUIDADORA';
}
