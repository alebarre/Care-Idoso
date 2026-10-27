/** Usuário do sistema conforme o backend. */
export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: 'ADMIN' | 'CUIDADORA';
  ativo?: boolean;
  foto?: string | null;
}

/**
 * Edição do próprio perfil. Campos omitidos não são alterados;
 * `novaSenha` exige `senhaAtual`.
 */
export interface AtualizarPerfilRequest {
  nome?: string;
  email?: string;
  senhaAtual?: string;
  novaSenha?: string;
}

/** Perfil atualizado + novo access token (o `sub` do JWT acompanha o e-mail). */
export interface PerfilResponse {
  usuario: Usuario;
  accessToken: string;
}
