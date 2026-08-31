import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LembretesComponent } from './pages/lembretes/lembretes.component';

const routes: Routes = [
  { path: '', component: LembretesComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LembretesRoutingModule { }
