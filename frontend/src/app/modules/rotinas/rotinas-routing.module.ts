import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RotinasComponent } from './pages/rotinas/rotinas.component';

const routes: Routes = [
  { path: '', component: RotinasComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RotinasRoutingModule { }
