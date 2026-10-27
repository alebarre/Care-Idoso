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

/** Criação de usuário por um ADMIN (`POST /api/usuarios`). */
export interface CriarUsuarioRequest {
  nome: string;
  email: string;
  senha: string;
  perfil: 'ADMIN' | 'CUIDADORA';
  ativo: boolean;
}

/** Edição de usuário por um ADMIN (`PUT /api/usuarios/{id}`); senha e status têm fluxos próprios. */
export interface EditarUsuarioRequest {
  nome: string;
  email: string;
  perfil: 'ADMIN' | 'CUIDADORA';
}
