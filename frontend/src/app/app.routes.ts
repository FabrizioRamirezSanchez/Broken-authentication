import { Routes } from '@angular/router';
import { Allinonelogin } from './feature/allinonelogin/allinonelogin';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login',
  },
  {
    path: 'login',
    component: Allinonelogin
  }
];
