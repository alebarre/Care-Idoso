import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';

interface ItemNavegacao {
  path: string;
  icone: string;
  label: string;
  exato?: boolean;
  somenteAdmin?: boolean;
}

const ITENS_NAVEGACAO: ItemNavegacao[] = [
  { path: '/', icone: 'house', label: 'Dashboard', exato: true },
  { path: '/rotinas', icone: 'clipboard-list', label: 'Rotinas' },
  { path: '/lembretes', icone: 'bell', label: 'Lembretes' },
  { path: '/medicamentos', icone: 'pills', label: 'Medicamentos' },
  { path: '/pedidos', icone: 'shopping-cart', label: 'Pedidos' },
  { path: '/idosos', icone: 'user-injured', label: 'Idosos' },
  { path: '/usuarios', icone: 'users', label: 'Usuários', somenteAdmin: true }
];

@Component({
  selector: 'app-shell',
  templateUrl: './shell.component.html',
  standalone: false,
  styleUrl: './shell.component.scss'
})
export class ShellComponent implements OnInit {
  isLoginPage = false;
  usuarioLogado$: typeof this.authService.usuarioLogado$;

  constructor(public authService: AuthService, private router: Router) {
    this.usuarioLogado$ = this.authService.usuarioLogado$;
  }

  ngOnInit(): void {
    this.isLoginPage = this.ehPaginaDeLogin(this.router.url);

    this.router.events
      .pipe(filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd))
      .subscribe(evento => {
        this.isLoginPage = this.ehPaginaDeLogin(evento.urlAfterRedirects);
      });
  }

  get itensNavegacao(): ItemNavegacao[] {
    return ITENS_NAVEGACAO.filter(item => !item.somenteAdmin || this.authService.isAdmin());
  }

  logout(): void {
    this.authService.logout();
  }

  private ehPaginaDeLogin(url: string): boolean {
    return url.startsWith('/login');
  }
}
