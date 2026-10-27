import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CriarUsuarioRequest, EditarUsuarioRequest, Usuario } from '../models/usuario.model';

/**
 * Gestão de usuários pelo ADMIN. O backend restringe todas as rotas a `ADMIN`;
 * a restrição de rota no frontend (`AdminGuard`) é só conveniência de UX.
 * Para o perfil do próprio usuário use `AuthService`.
 */
@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private readonly apiUrl = '/api/usuarios';

  constructor(private http: HttpClient) { }

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.apiUrl);
  }

  criar(request: CriarUsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.apiUrl, request);
  }

  editar(id: number, request: EditarUsuarioRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.apiUrl}/${id}`, request);
  }

  /** Desativar também encerra as sessões do usuário no backend. */
  alterarStatus(id: number, ativo: boolean): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.apiUrl}/${id}/status`, { ativo });
  }

  /** Exclusão lógica: o histórico de registros do usuário é preservado. */
  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
