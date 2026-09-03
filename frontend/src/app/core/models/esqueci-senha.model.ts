export interface EsqueciSenhaRequest {
  email: string;
}

export interface RedefinirSenhaRequest {
  email: string;
  codigo: string;
  novaSenha: string;
}

export interface NovaContaRequest {
  nome: string;
  email: string;
  senha: string;
  perfil?: 'ADMIN' | 'CUIDADORA';
}

export interface ValidarCodigoRequest {
  email: string;
  codigo: string;
}

export interface ValidarCodigoGenericoRequest {
  email: string;
  codigo: string;
  tipo: 'ESQUECI_SENHA' | 'NOVA_CONTA';
}

export interface MensagemResponse {
  mensagem: string;
}
