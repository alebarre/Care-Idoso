import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { RotinasRoutingModule } from './rotinas-routing.module';
import { RotinasComponent } from './pages/rotinas/rotinas.component';

@NgModule({
  declarations: [RotinasComponent],
  imports: [
    SharedModule,
    RotinasRoutingModule
  ]
})
export class RotinasModule { }
