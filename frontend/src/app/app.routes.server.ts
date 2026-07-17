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
    path: '**',
    renderMode: RenderMode.Prerender,
  },
];
