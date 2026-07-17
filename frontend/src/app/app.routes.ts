import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./paginas/home/home').then((m) => m.HomePage),
    title: 'Crear Code Cesar — Software a la medida, IA y soluciones tecnológicas para pymes',
  },
  {
    path: 'servicios/:slug',
    loadComponent: () => import('./paginas/servicio/servicio').then((m) => m.ServicioPage),
    title: 'Servicios — Crear Code Cesar',
  },
  {
    path: 'casos',
    loadComponent: () => import('./paginas/casos-listado/casos-listado').then((m) => m.CasosListadoPage),
    title: 'Casos de éxito — Crear Code Cesar',
  },
  {
    path: 'casos/:slug',
    loadComponent: () => import('./paginas/caso-detalle/caso-detalle').then((m) => m.CasoDetallePage),
    title: 'Caso de éxito — Crear Code Cesar',
  },
];
