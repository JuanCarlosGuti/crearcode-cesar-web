import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import compression from 'compression';
import express from 'express';
import { createProxyMiddleware } from 'http-proxy-middleware';
import { join } from 'node:path';

import { generarRobotsTxt } from './servidor/robots';
import { generarSitemap } from './servidor/sitemap';

const browserDistFolder = join(import.meta.dirname, '../browser');

const app = express();
const angularApp = new AngularNodeAppEngine();

/**
 * Compresión gzip/brotli de todas las respuestas — sin esto los bundles
 * de Angular (JS/CSS) y el HTML de SSR viajan sin comprimir, lo que
 * penaliza FCP/LCP en Lighthouse (ISS-077) y en redes móviles reales.
 */
app.use(compression());

/**
 * Proxy servidor-a-servidor hacia el backend real (ver ADR-09 en
 * docs/02-arquitectura.md): en producción, frontend y backend viven en
 * orígenes distintos (Render), así que el navegador nunca llama al
 * backend directo — solo a este mismo origen. Esto evita configurar CORS
 * por completo y no exige tocar `SolicitudesApi`/`AuthApi`, que ya usan
 * rutas relativas (`/api/...`) igual que con el proxy de `ng serve` en
 * desarrollo local.
 *
 * `pathFilter` en vez de montar con `app.use('/api', ...)`: Express le
 * recorta el prefijo `/api` a `req.url` antes de que la lógica de proxy
 * lo vea, y a partir de v3 `http-proxy-middleware` ya no lo reconstruye
 * solo — reenviaba `/solicitudes` en vez de `/api/solicitudes`, y el
 * backend lo rechazaba con 401 al no matchear ninguna ruta pública.
 */
const backendUrl = process.env['BACKEND_URL'] ?? 'http://localhost:8080';
app.use(createProxyMiddleware({ pathFilter: '/api', target: backendUrl, changeOrigin: true }));

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
