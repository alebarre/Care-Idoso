import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { LembretesRoutingModule } from './lembretes-routing.module';
import { LembretesComponent } from './pages/lembretes/lembretes.component';

@NgModule({
  declarations: [LembretesComponent],
  imports: [
    SharedModule,
    LembretesRoutingModule
  ]
})
export class LembretesModule { }
