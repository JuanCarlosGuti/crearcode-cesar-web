import { RenderMode, ServerRoute } from '@angular/ssr';

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
    path: '**',
    renderMode: RenderMode.Prerender,
  },
];
