import { ChangeDetectorRef, Component, Input, OnChanges, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';

/**
 * Mostra a foto do usuário (buscada com o JWT, pois o endpoint é autenticado)
 * ou a inicial do nome quando não há foto.
 *
 * Inputs: `nome` (fonte da inicial), `foto` (URL da API) e `tamanho` em px.
 * A imagem vira um object URL (blob) que é revogado ao trocar/destruir,
 * evitando vazamento de memória.
 *
 * O app roda sem zone.js: a resposta HTTP assíncrona não dispara detecção de
 * mudanças sozinha, por isso `markForCheck()` é chamado ao atualizar `fotoSrc`
 * (sem isso a foto só aparecia quando outro evento forçava um ciclo).
 */
@Component({
  selector: 'app-avatar',
  templateUrl: './avatar.component.html',
  standalone: false,
  styleUrl: './avatar.component.scss'
})
export class AvatarComponent implements OnChanges, OnDestroy {
  @Input() nome: string | null | undefined = '';
  @Input() foto: string | null | undefined = null;
  /** Lado do avatar em pixels. */
  @Input() tamanho = 48;

  fotoSrc: string | null = null;

  private requisicao?: Subscription;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) { }

  get inicial(): string {
    return (this.nome ?? '').trim().charAt(0).toUpperCase() || '?';
  }

  ngOnChanges(): void {
    this.requisicao?.unsubscribe();
    if (!this.foto) {
      this.liberarFoto();
      return;
    }

    this.requisicao = this.http.get(this.foto, { responseType: 'blob' }).subscribe({
      next: blob => {
        this.liberarFoto();
        this.fotoSrc = URL.createObjectURL(blob);
        this.cdr.markForCheck();
      },
      error: () => {
        this.liberarFoto();
        this.cdr.markForCheck();
      }
    });
  }

  ngOnDestroy(): void {
    this.requisicao?.unsubscribe();
    this.liberarFoto();
  }

  private liberarFoto(): void {
    if (this.fotoSrc) {
      URL.revokeObjectURL(this.fotoSrc);
    }
    this.fotoSrc = null;
  }
}
