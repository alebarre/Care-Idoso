import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

/**
 * Restringe rotas ao perfil `ADMIN`; os demais voltam para `/`.
 * Proteção de UX apenas: o backend reforça a role em cada endpoint.
 * Deve ser usado depois do `AuthGuard`.
 */
@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) { }

  canActivate(): boolean {
    if (this.authService.isAdmin()) {
      return true;
    }
    this.router.navigate(['/']);
    return false;
  }
}
