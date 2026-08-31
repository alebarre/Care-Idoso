import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IdosoListaComponent } from './pages/idoso-lista/idoso-lista.component';

const routes: Routes = [
  { path: '', component: IdosoListaComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class IdososRoutingModule { }
