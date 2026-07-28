import { RenderMode, ServerRoute } from '@angular/ssr';

import { ARTICULOS } from '../contenido/blog';
import { CASOS } from '../contenido/casos';
import { SERVICIOS } from '../contenido/servicios';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'servicios/:slug',
    renderMode: RenderMode.Prerender,
    async getPrerenderParams() {
      return SERVICIOS.map((servicio) => ({ slug: servicio.slug }));
    },
  },
  {
    path: 'casos/:slug',
    renderMode: RenderMode.Prerender,
    async getPrerenderParams() {
      return CASOS.map((caso) => ({ slug: caso.slug }));
    },
  },
  {
    path: 'blog/:slug',
    renderMode: RenderMode.Prerender,
    async getPrerenderParams() {
      return ARTICULOS.map((articulo) => ({ slug: articulo.slug }));
    },
  },
  {
    // Panel admin: sin SSR/prerender. No aporta SEO, esta detras de
    // login, y no hay token disponible en build (ver ADR-08 en
    // docs/02-arquitectura.md).
    path: 'admin/**',
    renderMode: RenderMode.Client,
  },
  {
    // Destino del enlace de verificacion (?token=): la llamada tiene
    // efectos y depende de la URL, nada que prerenderizar.
    path: 'verificar-correo',
    renderMode: RenderMode.Client,
  },
  {
    path: 'restablecer-contrasena',
    renderMode: RenderMode.Client,
  },
  {
    // Area del cliente: detras de clienteGuard, sin valor SEO.
    path: 'mi-cuenta',
    renderMode: RenderMode.Client,
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender,
  },
];
