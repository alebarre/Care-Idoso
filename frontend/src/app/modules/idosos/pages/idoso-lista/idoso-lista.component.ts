import { Component } from '@angular/core';

@Component({
  selector: 'app-idoso-lista',
  templateUrl: './idoso-lista.component.html',
  standalone: false,
  styleUrl: './idoso-lista.component.scss'
})
export class IdosoListaComponent {
  idosos: any[] = [];
}
