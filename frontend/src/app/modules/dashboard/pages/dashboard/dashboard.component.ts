import { Component } from '@angular/core';
import { AuthService } from '../../../../core/auth/auth.service';

/** Página inicial pós-login; expõe `authService` ao template para saudação e atalhos por perfil. */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  standalone: false,
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  constructor(public authService: AuthService) { }

  logout(): void {
    this.authService.logout();
  }
}
