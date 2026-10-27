import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { AdminGuard } from './core/guards/admin.guard';

/**
 * Rotas raiz, todas lazy. `login` é pública; as demais exigem `AuthGuard`,
 * e `usuarios` também `AdminGuard`. Rota desconhecida volta ao dashboard.
 */
const routes: Routes = [
  {
    path: 'login',
    loadChildren: () => import('./modules/login/login.module').then(m => m.LoginModule)
  },
  {
    path: '',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/dashboard/dashboard.module').then(m => m.DashboardModule)
  },
  {
    path: 'idosos',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/idosos/idosos.module').then(m => m.IdososModule)
  },
  {
    path: 'rotinas',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/rotinas/rotinas.module').then(m => m.RotinasModule)
  },
  {
    path: 'medicamentos',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/medicamentos/medicamentos.module').then(m => m.MedicamentosModule)
  },
  {
    path: 'lembretes',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/lembretes/lembretes.module').then(m => m.LembretesModule)
  },
  {
    path: 'pedidos',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/pedidos/pedidos.module').then(m => m.PedidosModule)
  },
  {
    path: 'perfil',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/perfil/perfil.module').then(m => m.PerfilModule)
  },
  {
    path: 'usuarios',
    canActivate: [AuthGuard, AdminGuard],
    loadChildren: () => import('./modules/usuarios/usuarios.module').then(m => m.UsuariosModule)
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
