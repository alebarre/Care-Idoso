import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { IdososRoutingModule } from './idosos-routing.module';
import { IdosoListaComponent } from './pages/idoso-lista/idoso-lista.component';

@NgModule({
  declarations: [IdosoListaComponent],
  imports: [
    SharedModule,
    IdososRoutingModule
  ]
})
export class IdososModule { }
