import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

/**
 * Protege rotas privadas: sem sessão (access ou refresh token), redireciona para `/login`.
 * Não valida o token; a expiração é tratada pelo `JwtInterceptor` ao chamar a API.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) { }

  canActivate(): boolean {
    if (this.authService.estaLogado()) {
      return true;
    }
    this.router.navigate(['/login']);
    return false;
  }
}
