import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { join } from 'node:path';

import { generarRobotsTxt } from './servidor/robots';
import { generarSitemap } from './servidor/sitemap';

const browserDistFolder = join(import.meta.dirname, '../browser');

const app = express();
const angularApp = new AngularNodeAppEngine();

/**
 * sitemap.xml generado dinámicamente desde el mismo contenido usado para
 * el prerender (ver `servidor/sitemap.ts`) — excluye siempre `/admin/**`
 * (HU-23).
 */
app.get('/sitemap.xml', (req, res) => {
  res.type('application/xml').send(generarSitemap());
});

/**
 * robots.txt generado dinámicamente (en vez de un archivo estático en
 * `public/`) para que la URL base salga siempre de `contenido/sitio.ts`,
 * nunca hardcodeada en dos lugares (ADR-06).
 */
app.get('/robots.txt', (req, res) => {
  res.type('text/plain').send(generarRobotsTxt());
});

/**
 * Serve static files from /browser
 */
app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

/**
 * Handle all other requests by rendering the Angular application.
 */
app.use((req, res, next) => {
  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

/**
 * Start the server if this module is the main entry point, or it is ran via PM2.
 * The server listens on the port defined by the `PORT` environment variable, or defaults to 4000.
 */
if (isMainModule(import.meta.url) || process.env['pm_id']) {
  const port = process.env['PORT'] || 4000;
  app.listen(port, (error) => {
    if (error) {
      throw error;
    }

    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

/**
 * Request handler used by the Angular CLI (for dev-server and during build) or Firebase Cloud Functions.
 */
export const reqHandler = createNodeRequestHandler(app);
