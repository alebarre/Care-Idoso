import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { FontAwesomeModule, FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { fas } from '@fortawesome/free-solid-svg-icons';

import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import { ToolbarModule } from 'primeng/toolbar';
import { MenuModule } from 'primeng/menu';
import { PanelModule } from 'primeng/panel';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { TextareaModule } from 'primeng/textarea';
import { BadgeModule } from 'primeng/badge';
import { MessageService } from 'primeng/api';
import { AvatarComponent } from './components/avatar/avatar.component';

/**
 * Módulo compartilhado: reexporta módulos comuns do Angular, componentes PrimeNG
 * usados no app e FontAwesome, para que módulos de funcionalidade importem só este.
 * Registra o pacote de ícones `fas` e provê o `MessageService` (toasts).
 */
@NgModule({
  declarations: [AvatarComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    FontAwesomeModule,
    ButtonModule,
    InputTextModule,
    PasswordModule,
    CardModule,
    TableModule,
    ToastModule,
    ToolbarModule,
    MenuModule,
    PanelModule,
    DialogModule,
    SelectModule,
    DatePickerModule,
    TextareaModule,
    BadgeModule
  ],
  exports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    FontAwesomeModule,
    ButtonModule,
    InputTextModule,
    PasswordModule,
    CardModule,
    TableModule,
    ToastModule,
    ToolbarModule,
    MenuModule,
    PanelModule,
    DialogModule,
    SelectModule,
    DatePickerModule,
    TextareaModule,
    BadgeModule,
    AvatarComponent
  ],
  providers: [MessageService]
})
export class SharedModule {
  constructor(library: FaIconLibrary) {
    // Registra todos os ícones sólidos; referencie por nome (ex.: `icon="house"`).
    library.addIconPacks(fas);
  }
}
