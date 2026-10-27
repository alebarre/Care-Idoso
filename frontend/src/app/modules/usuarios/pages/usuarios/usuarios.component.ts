import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../../../core/auth/auth.service';
import { UsuarioService } from '../../../../core/services/usuario.service';
import { Usuario } from '../../../../core/models/usuario.model';

type AcaoConfirmavel = 'desativar' | 'excluir';

interface Confirmacao {
  acao: AcaoConfirmavel;
  usuario: Usuario;
}

/**
 * Gestão de usuários (somente ADMIN): lista com busca, criar/editar em formulário
 * na própria tela, ativar/desativar e excluir, com confirmação para ações destrutivas.
 * Regras como "não desativar/excluir a si mesmo" são aplicadas no backend; aqui os
 * botões correspondentes são apenas ocultados para o usuário logado.
 */
@Component({
  selector: 'app-usuarios',
  templateUrl: './usuarios.component.html',
  standalone: false,
  styleUrl: './usuarios.component.scss'
})
export class UsuariosComponent implements OnInit, OnDestroy {
  usuarios: Usuario[] = [];
  busca = '';
  carregando = true;

  /** `null` = mostrando a lista; `'novo'` ou um usuário = formulário de criação/edição. */
  editando: Usuario | 'novo' | null = null;
  form!: FormGroup;
  salvando = false;

  confirmacao: Confirmacao | null = null;
  processando = false;

  meuId: number | null = null;
  private sessao?: Subscription;

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private authService: AuthService,
    private messageService: MessageService
  ) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
      perfil: ['CUIDADORA', Validators.required],
      senha: ['']
    });

    this.sessao = this.authService.usuarioLogado$.subscribe(u => this.meuId = u?.usuarioId ?? null);
    this.carregar();
  }

  ngOnDestroy(): void {
    this.sessao?.unsubscribe();
  }

  get filtrados(): Usuario[] {
    const termo = this.busca.trim().toLowerCase();
    if (!termo) {
      return this.usuarios;
    }
    return this.usuarios.filter(u =>
      `${u.nome} ${u.email} ${u.perfil}`.toLowerCase().includes(termo)
    );
  }

  get ehNovo(): boolean {
    return this.editando === 'novo';
  }

  ehEu(usuario: Usuario): boolean {
    return usuario.id === this.meuId;
  }

  carregar(): void {
    this.carregando = true;
    this.usuarioService.listar().subscribe({
      next: usuarios => {
        this.usuarios = usuarios;
        this.carregando = false;
      },
      error: (e: HttpErrorResponse) => {
        this.carregando = false;
        this.erro(e, 'Não foi possível carregar os usuários.');
      }
    });
  }

  abrirFormulario(usuario?: Usuario): void {
    this.editando = usuario ?? 'novo';
    this.form.reset({
      nome: usuario?.nome ?? '',
      email: usuario?.email ?? '',
      perfil: usuario?.perfil ?? 'CUIDADORA',
      senha: ''
    });
    // senha só existe na criação; na edição é alterada pelo próprio usuário / redefinição
    const senha = this.form.get('senha')!;
    senha.setValidators(usuario ? [] : [Validators.required, Validators.minLength(6), Validators.maxLength(100)]);
    senha.updateValueAndValidity();
    // o ADMIN não pode mudar o próprio perfil (o backend também recusa)
    if (usuario && this.ehEu(usuario)) {
      this.form.get('perfil')!.disable();
    } else {
      this.form.get('perfil')!.enable();
    }
  }

  fecharFormulario(): void {
    this.editando = null;
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nome, email, senha } = this.form.value;
    const perfil = this.form.getRawValue().perfil;
    this.salvando = true;

    const operacao = this.editando === 'novo'
      ? this.usuarioService.criar({ nome, email, senha, perfil, ativo: true })
      : this.usuarioService.editar((this.editando as Usuario).id, { nome, email, perfil });

    operacao.subscribe({
      next: () => {
        this.salvando = false;
        this.messageService.add({
          severity: 'success',
          summary: this.ehNovo ? 'Usuário criado' : 'Usuário atualizado',
          detail: 'As alterações foram salvas.'
        });
        this.editando = null;
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.salvando = false;
        this.erro(e, 'Não foi possível salvar o usuário.');
      }
    });
  }

  /** Ativar é imediato; desativar pede confirmação (encerra as sessões do usuário). */
  alternarStatus(usuario: Usuario): void {
    if (usuario.ativo) {
      this.confirmacao = { acao: 'desativar', usuario };
      return;
    }
    this.aplicarStatus(usuario, true);
  }

  pedirExclusao(usuario: Usuario): void {
    this.confirmacao = { acao: 'excluir', usuario };
  }

  cancelarConfirmacao(): void {
    if (!this.processando) {
      this.confirmacao = null;
    }
  }

  confirmar(): void {
    if (!this.confirmacao) {
      return;
    }
    const { acao, usuario } = this.confirmacao;
    if (acao === 'desativar') {
      this.aplicarStatus(usuario, false);
    } else {
      this.excluir(usuario);
    }
  }

  private aplicarStatus(usuario: Usuario, ativo: boolean): void {
    this.processando = true;
    this.usuarioService.alterarStatus(usuario.id, ativo).subscribe({
      next: atualizado => {
        this.processando = false;
        this.confirmacao = null;
        this.usuarios = this.usuarios.map(u => u.id === atualizado.id ? atualizado : u);
        this.messageService.add({
          severity: 'success',
          summary: ativo ? 'Usuário ativado' : 'Usuário desativado',
          detail: ativo ? 'O acesso foi liberado.' : 'O acesso foi bloqueado.'
        });
      },
      error: (e: HttpErrorResponse) => {
        this.processando = false;
        this.confirmacao = null;
        this.erro(e, 'Não foi possível alterar o status.');
      }
    });
  }

  private excluir(usuario: Usuario): void {
    this.processando = true;
    this.usuarioService.excluir(usuario.id).subscribe({
      next: () => {
        this.processando = false;
        this.confirmacao = null;
        this.usuarios = this.usuarios.filter(u => u.id !== usuario.id);
        this.messageService.add({ severity: 'success', summary: 'Usuário excluído', detail: 'O cadastro foi removido.' });
      },
      error: (e: HttpErrorResponse) => {
        this.processando = false;
        this.confirmacao = null;
        this.erro(e, 'Não foi possível excluir o usuário.');
      }
    });
  }

  private erro(e: HttpErrorResponse, padrao: string): void {
    this.messageService.add({ severity: 'error', summary: 'Erro', detail: e.error?.mensagem ?? padrao });
  }
}
