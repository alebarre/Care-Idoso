import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../../../core/auth/auth.service';
import { AtualizarPerfilRequest, Usuario } from '../../../../core/models/usuario.model';

const TIPOS_FOTO = ['image/jpeg', 'image/png'];
const TAMANHO_MAXIMO_FOTO = 2 * 1024 * 1024;

@Component({
  selector: 'app-perfil',
  templateUrl: './perfil.component.html',
  standalone: false,
  styleUrl: './perfil.component.scss'
})
export class PerfilComponent implements OnInit {
  form!: FormGroup;
  usuario: Usuario | null = null;
  salvando = false;
  enviandoFoto = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private messageService: MessageService
  ) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
      senhaAtual: [''],
      novaSenha: ['', [Validators.minLength(6), Validators.maxLength(100)]]
    }, { validators: this.senhaAtualObrigatoria });

    this.authService.carregarPerfil().subscribe({
      next: usuario => {
        this.usuario = usuario;
        this.form.patchValue({ nome: usuario.nome, email: usuario.email });
      },
      error: () => this.erro('Não foi possível carregar seu perfil.')
    });
  }

  get fotoAtual(): string | null {
    return this.usuario?.foto ?? null;
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nome, email, senhaAtual, novaSenha } = this.form.value;
    const request: AtualizarPerfilRequest = { nome, email };
    if (novaSenha) {
      request.senhaAtual = senhaAtual;
      request.novaSenha = novaSenha;
    }

    this.salvando = true;
    this.authService.atualizarPerfil(request).subscribe({
      next: resposta => {
        this.salvando = false;
        this.usuario = resposta.usuario;
        this.form.patchValue({ senhaAtual: '', novaSenha: '' });
        this.form.markAsPristine();
        this.messageService.add({ severity: 'success', summary: 'Perfil atualizado', detail: 'Seus dados foram salvos.' });
      },
      error: (e: HttpErrorResponse) => {
        this.salvando = false;
        this.erro(e.error?.mensagem ?? 'Não foi possível salvar o perfil.');
      }
    });
  }

  aoEscolherFoto(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const arquivo = input.files?.[0];
    input.value = '';
    if (!arquivo) {
      return;
    }

    if (!TIPOS_FOTO.includes(arquivo.type)) {
      this.erro('Formato inválido. Envie uma imagem JPG ou PNG.');
      return;
    }
    if (arquivo.size > TAMANHO_MAXIMO_FOTO) {
      this.erro('A foto deve ter no máximo 2 MB.');
      return;
    }

    this.enviandoFoto = true;
    this.authService.enviarFoto(arquivo).subscribe({
      next: usuario => {
        this.enviandoFoto = false;
        this.usuario = usuario;
        this.messageService.add({ severity: 'success', summary: 'Foto atualizada', detail: 'Sua foto de perfil foi salva.' });
      },
      error: (e: HttpErrorResponse) => {
        this.enviandoFoto = false;
        this.erro(e.error?.mensagem ?? 'Não foi possível enviar a foto.');
      }
    });
  }

  mostrarErro(campo: string): boolean {
    const controle = this.form.get(campo);
    return !!controle && controle.invalid && (controle.touched || controle.dirty);
  }

  private senhaAtualObrigatoria(grupo: AbstractControl): ValidationErrors | null {
    const nova = grupo.get('novaSenha')?.value;
    const atual = grupo.get('senhaAtual')?.value;
    return nova && !atual ? { senhaAtualObrigatoria: true } : null;
  }

  private erro(detail: string): void {
    this.messageService.add({ severity: 'error', summary: 'Erro', detail });
  }
}
