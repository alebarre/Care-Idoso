import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';
import { MessageService } from 'primeng/api';

/**
 * Tela de login. Em sucesso, `AuthService` já persistiu os tokens e a
 * navegação vai para `/`. Falhas exibem mensagem genérica (sem revelar
 * se o e-mail existe).
 */
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone: false,
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  carregando = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService
  ) { }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required, Validators.minLength(4)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.carregando = true;
    const { email, senha } = this.loginForm.value;

    this.authService.login(email, senha).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.carregando = false;
        // 403 = conta inativa (credenciais corretas); 429 = rate limit: o backend traz a mensagem
        const detail = (err?.status === 403 || err?.status === 429) && err?.error?.mensagem
          ? err.error.mensagem
          : 'E-mail ou senha inválidos.';
        this.messageService.add({
          severity: 'error',
          summary: 'Erro',
          detail
        });
      }
    });
  }
}
