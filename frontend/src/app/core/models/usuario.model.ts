export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: 'ADMIN' | 'CUIDADORA';
  ativo?: boolean;
  foto?: string | null;
}

export interface AtualizarPerfilRequest {
  nome?: string;
  email?: string;
  senhaAtual?: string;
  novaSenha?: string;
}

export interface PerfilResponse {
  usuario: Usuario;
  accessToken: string;
}
