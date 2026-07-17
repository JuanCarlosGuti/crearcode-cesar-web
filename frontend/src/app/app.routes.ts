import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./paginas/home/home').then((m) => m.HomePage),
    title: 'Crear Code Cesar — Software a la medida, IA y soluciones tecnológicas para pymes',
  },
];
