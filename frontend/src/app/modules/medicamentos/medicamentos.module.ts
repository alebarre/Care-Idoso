import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { MedicamentosRoutingModule } from './medicamentos-routing.module';
import { MedicamentosComponent } from './pages/medicamentos/medicamentos.component';

@NgModule({
  declarations: [MedicamentosComponent],
  imports: [
    SharedModule,
    MedicamentosRoutingModule
  ]
})
export class MedicamentosModule { }
