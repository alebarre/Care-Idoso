import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { UsuariosRoutingModule } from './usuarios-routing.module';
import { UsuariosComponent } from './pages/usuarios/usuarios.component';

@NgModule({
  declarations: [UsuariosComponent],
  imports: [
    SharedModule,
    UsuariosRoutingModule
  ]
})
export class UsuariosModule { }
