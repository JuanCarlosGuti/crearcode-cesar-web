import { mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

import { generarRobotsTxt } from '../src/servidor/robots';
import { generarSitemap } from '../src/servidor/sitemap';

/**
 * Escribe sitemap.xml y robots.txt junto al resto del build (ADR-12).
 * Antes los generaba el servidor Node en cada petición; al pasar a
 * Static Site no hay servidor, pero **la URL base sigue saliendo de
 * `contenido/sitio.ts`** (ADR-06): este script importa las mismas
 * funciones que ya tenían sus tests, así que no se duplica el dominio.
 *
 * Se ejecuta tras `ng build` (ver el script `build` de package.json),
 * empaquetado con esbuild para resolver los imports de TypeScript.
 */
const destino = join(import.meta.dirname, '..', 'dist', 'frontend', 'browser');

mkdirSync(destino, { recursive: true });
writeFileSync(join(destino, 'sitemap.xml'), generarSitemap(), 'utf8');
writeFileSync(join(destino, 'robots.txt'), generarRobotsTxt(), 'utf8');

console.log('Generados sitemap.xml y robots.txt en', destino);
