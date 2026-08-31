import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { PedidosRoutingModule } from './pedidos-routing.module';
import { PedidosComponent } from './pages/pedidos/pedidos.component';

@NgModule({
  declarations: [PedidosComponent],
  imports: [
    SharedModule,
    PedidosRoutingModule
  ]
})
export class PedidosModule { }
