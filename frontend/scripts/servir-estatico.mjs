// Sirve el build estatico como lo hara Render (ADR-12), para poder
// auditar con Lighthouse el artefacto real de produccion.
//
// Replica las dos reglas que importan: si el archivo existe se sirve
// tal cual (las 19 rutas prerenderizadas), y si no, cae al shell de SPA
// index.csr.html — NO a index.html, que es la Home prerenderizada.
//
// Uso: node scripts/servir-estatico.mjs [puerto]

import { createServer } from 'node:http';
import { existsSync, readFileSync, statSync } from 'node:fs';
import { extname, join, normalize } from 'node:path';
import { gzipSync } from 'node:zlib';

const RAIZ = join(import.meta.dirname, '..', 'dist', 'frontend', 'browser');
const PUERTO = Number(process.argv[2] ?? 4300);

const TIPOS = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.xml': 'application/xml; charset=utf-8',
  '.txt': 'text/plain; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.jpg': 'image/jpeg',
  '.png': 'image/png',
  '.ico': 'image/x-icon',
  '.woff2': 'font/woff2',
};

function archivoPara(ruta) {
  // normalize + prefijo: evita salir de la raiz con ../
  const candidato = join(RAIZ, normalize(ruta).replace(/^(\.\.[/\\])+/, ''));
  if (!candidato.startsWith(RAIZ)) {
    return null;
  }
  if (existsSync(candidato) && statSync(candidato).isFile()) {
    return candidato;
  }
  const conIndex = join(candidato, 'index.html');
  if (existsSync(conIndex)) {
    return conIndex;
  }
  return null;
}

createServer((peticion, respuesta) => {
  const ruta = decodeURIComponent(new URL(peticion.url, 'http://localhost').pathname);
  const archivo = archivoPara(ruta) ?? join(RAIZ, 'index.csr.html');

  const contenido = readFileSync(archivo);
  const cabeceras = {
    'Content-Type': TIPOS[extname(archivo)] ?? 'application/octet-stream',
    // La misma cabecera que emite el Static Site en produccion.
    'Strict-Transport-Security': 'max-age=86400; includeSubDomains',
  };

  // Comprimir NO es un detalle: el CDN de Render lo hace, y sin esto la
  // medicion de Lighthouse cae ~15 puntos de Performance por servir los
  // bundles en crudo, dando una falsa alarma de regresion.
  const aceptaGzip = (peticion.headers['accept-encoding'] ?? '').includes('gzip');
  const comprimible = /text|javascript|json|xml|svg/.test(cabeceras['Content-Type']);

  if (aceptaGzip && comprimible) {
    const comprimido = gzipSync(contenido);
    respuesta.writeHead(200, { ...cabeceras, 'Content-Encoding': 'gzip' });
    respuesta.end(comprimido);
    return;
  }

  respuesta.writeHead(200, cabeceras);
  respuesta.end(contenido);
}).listen(PUERTO, () => {
  console.log(`Estatico servido en http://localhost:${PUERTO} desde ${RAIZ}`);
});
