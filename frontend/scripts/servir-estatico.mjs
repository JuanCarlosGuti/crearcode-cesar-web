// Sirve el build estatico exactamente como lo hace Render (ADR-12),
// para poder auditar con Lighthouse y revisar a mano el artefacto real
// de produccion.
//
// Las reglas NO se reescriben aqui: se leen del render.yaml de verdad
// (scripts/rutas-de-render.mjs). La primera version de este servidor
// resolvia /contacto -> contacto/index.html por su cuenta, algo que
// Render no hace, y por eso el sitio se veia perfecto en local mientras
// en produccion todas las paginas devolvian el cascaron del SPA. Un
// replicador mas generoso que el original no verifica nada.
//
// Uso: node scripts/servir-estatico.mjs [puerto]

import { createServer } from 'node:http';
import { existsSync, readFileSync, statSync } from 'node:fs';
import { extname, join } from 'node:path';
import { gzipSync } from 'node:zlib';

import { leerReglas, resolver } from './rutas-de-render.mjs';

const RAIZ = join(import.meta.dirname, '..', 'dist', 'frontend', 'browser');
const PUERTO = Number(process.argv[2] ?? 4300);
const REGLAS = leerReglas();

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

function existe(ruta) {
  // El prefijo evita salir de la raiz con ../
  const candidato = join(RAIZ, ruta);
  return candidato.startsWith(RAIZ) && existsSync(candidato) && statSync(candidato).isFile();
}

createServer((peticion, respuesta) => {
  const ruta = decodeURIComponent(new URL(peticion.url, 'http://localhost').pathname);
  const destino = resolver(ruta, existe, REGLAS);

  // El backend no corre en esta replica; se avisa en vez de devolver
  // html, que es lo que confundiria a quien este depurando.
  if (destino.tipo === 'externo') {
    respuesta.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' });
    respuesta.end(JSON.stringify({ error: `Sin backend local. En produccion: ${destino.destino}` }));
    return;
  }

  if (destino.tipo === 'no-encontrado') {
    respuesta.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
    respuesta.end('404');
    return;
  }

  const contenido = readFileSync(join(RAIZ, destino.ruta));
  const cabeceras = {
    'Content-Type': TIPOS[extname(destino.ruta)] ?? 'application/octet-stream',
    // La misma cabecera que emite el Static Site en produccion.
    'Strict-Transport-Security': 'max-age=86400; includeSubDomains',
  };

  // Comprimir NO es un detalle: el CDN de Render lo hace, y sin esto la
  // medicion de Lighthouse cae ~15 puntos de Performance por servir los
  // bundles en crudo, dando una falsa alarma de regresion.
  const aceptaGzip = (peticion.headers['accept-encoding'] ?? '').includes('gzip');
  const comprimible = /text|javascript|json|xml|svg/.test(cabeceras['Content-Type']);

  if (aceptaGzip && comprimible) {
    respuesta.writeHead(200, { ...cabeceras, 'Content-Encoding': 'gzip' });
    respuesta.end(gzipSync(contenido));
    return;
  }

  respuesta.writeHead(200, cabeceras);
  respuesta.end(contenido);
}).listen(PUERTO, () => {
  console.log(`Estatico servido en http://localhost:${PUERTO} desde ${RAIZ}`);
});
