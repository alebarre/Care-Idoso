import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, Subscription, takeUntil, interval, finalize } from 'rxjs';
import { AuthService } from '../../../../core/auth/auth.service';
import { MessageService } from 'primeng/api';

/** Etapas do cadastro: dados -> código (OTP por e-mail) -> sucesso. */
type EtapaNovaConta = 'dados' | 'codigo' | 'sucesso';

/**
 * Cadastro público em duas fases: envia os dados (a API dispara o código por
 * e-mail) e valida o código de 5 dígitos. Contas criadas aqui são sempre
 * `CUIDADORA`: o request não carrega perfil e o backend define o papel.
 * O reenvio fica bloqueado por 60 s (`tempoRestante`).
 */
@Component({
  selector: 'app-nova-conta',
  templateUrl: './nova-conta.component.html',
  standalone: false,
  styleUrl: './nova-conta.component.scss'
})
export class NovaContaComponent implements OnInit, OnDestroy {
  etapa: EtapaNovaConta = 'dados';
  form!: FormGroup;
  codigoForm!: FormGroup;
  carregando = false;
  tempoRestante = 0;
  private destroy$ = new Subject<void>();
  private contadorSubscription: Subscription | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
    // `detectChanges` no finalize força a atualização do botão de carregando ao fim da requisição.
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
      senha: ['', [Validators.required, Validators.minLength(6), Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d).+$/)]],
      confirmarSenha: ['', [Validators.required]]
    }, { validators: this.senhasIguais });

    this.codigoForm = this.fb.group({
      codigo: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(5), Validators.pattern(/^\d{5}$/)]]
    });
  }

  ngOnDestroy(): void {
    this.pararContador();
    this.destroy$.next();
    this.destroy$.complete();
  }

  /** Validador de grupo: `senha` e `confirmarSenha` devem coincidir. */
  private senhasIguais(group: FormGroup) {
    const senha = group.get('senha')?.value;
    const confirmacao = group.get('confirmarSenha')?.value;
    return senha === confirmacao ? null : { senhasDiferentes: true };
  }

  solicitarCodigo(): void {
    if (this.form.invalid) {
      return;
    }

    this.carregando = true;
    const request = {
      nome: this.form.value.nome,
      email: this.form.value.email,
      senha: this.form.value.senha
    };

    this.authService.novaConta(request).pipe(
      finalize(() => {
        this.carregando = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.etapa = 'codigo';
        this.iniciarContador(60);
      },
      error: (err) => {
        const mensagem = err?.error?.mensagem || 'Não foi possível criar a conta. Tente novamente.';
        this.messageService.add({ severity: 'error', summary: 'Erro', detail: mensagem });
      }
    });
  }

  validarCodigo(): void {
    if (this.codigoForm.invalid) {
      return;
    }

    this.carregando = true;
    const request = {
      email: this.form.value.email,
      codigo: this.codigoForm.value.codigo
    };

    this.authService.validarCodigoNovaConta(request).pipe(
      finalize(() => {
        this.carregando = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.etapa = 'sucesso';
      },
      error: (err) => {
        const mensagem = err?.error?.mensagem || 'Código inválido ou expirado.';
        this.messageService.add({ severity: 'error', summary: 'Erro', detail: mensagem });
      }
    });
  }

  reenviarCodigo(): void {
    if (this.tempoRestante > 0) {
      return;
    }

    this.carregando = true;
    const request = {
      nome: this.form.value.nome,
      email: this.form.value.email,
      senha: this.form.value.senha
    };

    this.authService.novaConta(request).pipe(
      finalize(() => {
        this.carregando = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.iniciarContador(60);
        this.messageService.add({ severity: 'info', summary: 'Reenviado', detail: 'Um novo código foi enviado.' });
      },
      error: (err) => {
        const mensagem = err?.error?.mensagem || 'Não foi possível reenviar o código.';
        this.messageService.add({ severity: 'error', summary: 'Erro', detail: mensagem });
      }
    });
  }

  irParaLogin(): void {
    this.router.navigate(['/login']);
  }

  voltar(): void {
    if (this.etapa === 'codigo') {
      this.etapa = 'dados';
    }
  }

  /** Inicia a contagem regressiva (1 s) que bloqueia o reenvio do código. */
  private iniciarContador(segundos: number): void {
    this.pararContador();
    this.tempoRestante = segundos;
    this.contadorSubscription = interval(1000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (this.tempoRestante > 0) {
          this.tempoRestante--;
        } else {
          this.pararContador();
        }
      });
  }

  private pararContador(): void {
    if (this.contadorSubscription) {
      this.contadorSubscription.unsubscribe();
      this.contadorSubscription = null;
    }
  }
}
