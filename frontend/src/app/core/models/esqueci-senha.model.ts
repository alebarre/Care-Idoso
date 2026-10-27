/** Solicita o envio do código (OTP) de recuperação de senha. */
export interface EsqueciSenhaRequest {
  email: string;
}

/** Conclui a recuperação: exige o código previamente validado. */
export interface RedefinirSenhaRequest {
  email: string;
  codigo: string;
  novaSenha: string;
}

/** Cadastro público; a conta só é ativada após validar o código enviado por e-mail. */
export interface NovaContaRequest {
  nome: string;
  email: string;
  senha: string;
  perfil?: 'ADMIN' | 'CUIDADORA';
}

/** Validação do código (5 dígitos) do cadastro. */
export interface ValidarCodigoRequest {
  email: string;
  codigo: string;
}

/** Validação de código distinguindo o fluxo de origem via `tipo`. */
export interface ValidarCodigoGenericoRequest {
  email: string;
  codigo: string;
  tipo: 'ESQUECI_SENHA' | 'NOVA_CONTA';
}

/** Resposta textual padrão da API de autenticação. */
export interface MensagemResponse {
  mensagem: string;
}
