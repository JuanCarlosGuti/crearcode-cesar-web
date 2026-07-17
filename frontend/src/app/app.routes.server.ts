import { RenderMode, ServerRoute } from '@angular/ssr';

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
    path: '**',
    renderMode: RenderMode.Prerender,
  },
];
