import { Routes } from '@angular/router';

import { POLITICA_DE_DATOS, TERMINOS_DE_USO } from '../contenido/legales';
import { adminGuard } from './nucleo/admin.guard';

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
  {
    path: 'sobre-nosotros',
    loadComponent: () => import('./paginas/sobre-nosotros/sobre-nosotros').then((m) => m.SobreNosotrosPage),
    title: 'Sobre nosotros — Crear Code Cesar',
  },
  {
    path: 'blog',
    loadComponent: () => import('./paginas/blog-listado/blog-listado').then((m) => m.BlogListadoPage),
    title: 'Blog y recursos — Crear Code Cesar',
  },
  {
    path: 'blog/:slug',
    loadComponent: () => import('./paginas/blog-articulo/blog-articulo').then((m) => m.BlogArticuloPage),
    title: 'Blog — Crear Code Cesar',
  },
  {
    path: 'contacto',
    loadComponent: () => import('./paginas/contacto/contacto').then((m) => m.ContactoPage),
    title: 'Contacto — Crear Code Cesar',
  },
  {
    path: 'legales/politica-de-datos',
    loadComponent: () => import('./paginas/legal/legal').then((m) => m.LegalPage),
    data: { documento: POLITICA_DE_DATOS, ruta: '/legales/politica-de-datos' },
    title: 'Política de tratamiento de datos — Crear Code Cesar',
  },
  {
    path: 'legales/terminos',
    loadComponent: () => import('./paginas/legal/legal').then((m) => m.LegalPage),
    data: { documento: TERMINOS_DE_USO, ruta: '/legales/terminos' },
    title: 'Términos de uso — Crear Code Cesar',
  },
  {
    path: 'admin/login',
    loadComponent: () => import('./admin/login/login').then((m) => m.LoginPage),
    title: 'Ingresar — Panel Crear Code Cesar',
  },
  {
    path: 'admin',
    loadComponent: () =>
      import('./admin/listado-solicitudes/listado-solicitudes').then((m) => m.ListadoSolicitudesPage),
    canActivate: [adminGuard],
    title: 'Solicitudes — Panel Crear Code Cesar',
  },
  {
    path: 'admin/solicitudes/:id',
    loadComponent: () => import('./admin/detalle-solicitud/detalle-solicitud').then((m) => m.DetalleSolicitudPage),
    canActivate: [adminGuard],
    title: 'Solicitud — Panel Crear Code Cesar',
  },
];
