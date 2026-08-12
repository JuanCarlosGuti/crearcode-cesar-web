// Verifica que las reglas de render.yaml sirvan de verdad el sitio
// construido (ADR-12). Corre sobre el artefacto real, no sobre un
// modelo: para cada pagina prerenderizada comprueba que Render
// devolveria SU html y no el cascaron del SPA.
//
// Existe porque ese fallo es silencioso: todas las URLs responden 200 y
// el navegador termina pintando la pagina correcta con JavaScript, asi
// que solo se nota mirando el html de la primera respuesta — que es lo
// unico que ven Google y las tarjetas de WhatsApp/LinkedIn.
//
// Uso: node scripts/verificar-rutas-estaticas.mjs

import { existsSync, readFileSync, statSync } from 'node:fs';
import { join } from 'node:path';
import { globSync } from 'node:fs';

import { leerReglas, resolver } from './rutas-de-render.mjs';

const RAIZ = join(import.meta.dirname, '..', 'dist', 'frontend', 'browser');
const SHELL = '/index.csr.html';

const existe = (ruta) => {
  const candidato = join(RAIZ, ruta);
  return existsSync(candidato) && statSync(candidato).isFile();
};

const titulo = (ruta) =>
  /<title>([^<]*)<\/title>/.exec(readFileSync(join(RAIZ, ruta), 'utf8'))?.[1] ?? '';

if (!existsSync(RAIZ)) {
  console.error(`No hay build en ${RAIZ}. Corre "npm run build" primero.`);
  process.exit(1);
}

const reglas = leerReglas();
const fallos = [];
const comprobar = (caso, condicion, detalle) =>
  condicion ? null : fallos.push(`${caso}\n    ${detalle}`);

// 1. Cada pagina prerenderizada, pedida como la publica el sitemap (sin
//    barra final), debe servir su propio html.
const paginas = globSync('**/index.html', { cwd: RAIZ })
  // globSync devuelve separadores del sistema; las URLs siempre usan /.
  .map((archivo) => `/${archivo.replaceAll('\\', '/').replace(/index\.html$/, '')}`)
  .map((ruta) => (ruta === '/' ? '/' : ruta.replace(/\/$/, '')));

const tituloDelShell = titulo(SHELL);
for (const pagina of paginas) {
  const destino = resolver(pagina, existe, reglas);
  const sirve = destino.tipo === 'archivo' ? destino.ruta : destino.tipo;
  comprobar(
    `${pagina} debe servir su pagina prerenderizada`,
    destino.tipo === 'archivo' && destino.ruta !== SHELL,
    `sirvio: ${sirve}`,
  );
  if (destino.tipo === 'archivo' && destino.ruta !== SHELL) {
    comprobar(
      `${pagina} debe traer su propio <title> (SEO)`,
      pagina === '/' || titulo(destino.ruta) !== tituloDelShell,
      `title generico del shell: "${tituloDelShell}"`,
    );
  }
}

// 2. Las rutas de sesion (RenderMode.Client) no existen como archivo:
//    tienen que caer en el shell del SPA, nunca en la Home.
for (const ruta of [
  '/admin',
  '/admin/solicitudes/42',
  '/admin/cotizaciones',
  '/mi-cuenta',
  '/mi-cuenta/cotizaciones',
  '/verificar-correo',
  '/restablecer-contrasena',
]) {
  const destino = resolver(ruta, existe, reglas);
  comprobar(
    `${ruta} debe servir el shell del SPA`,
    destino.tipo === 'archivo' && destino.ruta === SHELL,
    `sirvio: ${destino.tipo === 'archivo' ? destino.ruta : destino.tipo}`,
  );
}

// 3. Archivos reales: se sirven tal cual, sin pasar por las reglas.
for (const archivo of ['/sitemap.xml', '/robots.txt', '/favicon.ico']) {
  const destino = resolver(archivo, existe, reglas);
  comprobar(
    `${archivo} debe servirse como archivo`,
    destino.tipo === 'archivo' && destino.ruta === archivo,
    `sirvio: ${destino.tipo === 'archivo' ? destino.ruta : destino.tipo}`,
  );
}

// 4. El /api sigue reenviado al backend (ADR-09: un solo origen, sin CORS).
const api = resolver('/api/solicitudes', existe, reglas);
comprobar(
  '/api/* debe reenviarse al backend',
  api.tipo === 'externo' && api.destino.endsWith('/api/solicitudes'),
  `resolvio: ${JSON.stringify(api)}`,
);

if (fallos.length) {
  console.error(`\n${fallos.length} fallo(s) de enrutamiento en render.yaml:\n`);
  fallos.forEach((fallo, i) => console.error(`  ${i + 1}. ${fallo}`));
  console.error('');
  process.exit(1);
}

console.log(`OK: ${paginas.length} paginas prerenderizadas, rutas de sesion, archivos y /api.`);
