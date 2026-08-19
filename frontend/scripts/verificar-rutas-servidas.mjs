// Verifica un sitio ya servido: pide cada ruta por HTTP y comprueba que
// la PRIMERA respuesta trae la pagina correcta — no que responda 200.
//
// Es la variante de verificar-rutas-estaticas.mjs para cuando el
// servidor ya no es Render: aquella lee las reglas de render.yaml, esta
// no sabe nada de reglas y solo mira lo que sale por el cable. Sirve
// igual para Caddy, para Render o para produccion.
//
// Con dos URLs compara lado a lado y muestra en que se desvia la
// primera de la segunda, ruta por ruta. Eso es lo util al migrar: no
// "algo fallo", sino "aqui Caddy no hace lo que hacia Render".
//
// Uso:
//   node scripts/verificar-rutas-servidas.mjs http://localhost:4310
//   node scripts/verificar-rutas-servidas.mjs http://localhost:4310 https://crearcodecesar.com

import { existsSync, globSync } from 'node:fs';
import { join } from 'node:path';

const RAIZ = join(import.meta.dirname, '..', 'dist', 'frontend', 'browser');
const [base, referencia] = process.argv.slice(2);

if (!base) {
  console.error('Falta la URL a verificar. Ej: node scripts/verificar-rutas-servidas.mjs http://localhost:4310');
  process.exit(1);
}
if (!existsSync(RAIZ)) {
  console.error(`No hay build en ${RAIZ}. Corre "npm run build" primero.`);
  process.exit(1);
}

// Las rutas salen del build real, no de una lista a mano: si manana hay
// una pagina nueva, entra sola en la verificacion.
const paginas = globSync('**/index.html', { cwd: RAIZ })
  .map((archivo) => `/${archivo.replaceAll('\\', '/').replace(/index\.html$/, '')}`)
  .map((ruta) => (ruta === '/' ? '/' : ruta.replace(/\/$/, '')))
  .sort();

const SESION = [
  '/admin',
  '/admin/solicitudes/42',
  '/admin/cotizaciones',
  '/mi-cuenta',
  '/mi-cuenta/cotizaciones',
  '/verificar-correo',
  '/restablecer-contrasena',
];
const ARCHIVOS = ['/sitemap.xml', '/robots.txt'];
const INEXISTENTE = '/esta-ruta-no-existe-jamas';

/**
 * Identidad de lo que devolvio el servidor. El marcador estable es el
 * <link rel="canonical">: el cascaron del SPA no lo trae, porque lo
 * pone Angular al renderizar. El <title> solo se usa para el informe.
 */
async function pedir(url) {
  try {
    const respuesta = await fetch(url, { redirect: 'manual' });
    const cuerpo = respuesta.headers.get('content-type')?.includes('html')
      ? await respuesta.text()
      : '';
    return {
      estado: respuesta.status,
      canonical: /<link[^>]+rel="canonical"[^>]+href="([^"]+)"/.exec(cuerpo)?.[1] ?? null,
      titulo: /<title>([^<]*)<\/title>/.exec(cuerpo)?.[1] ?? null,
      tipo: respuesta.headers.get('content-type')?.split(';')[0] ?? null,
    };
  } catch (error) {
    return { estado: 0, error: String(error.cause?.code ?? error.message) };
  }
}

const resumen = (r) =>
  r.estado === 0 ? `sin conexion (${r.error})` : `${r.estado} ${r.canonical ?? r.titulo ?? r.tipo ?? ''}`.trim();

const fallos = [];
const anotar = (ruta, detalle) => fallos.push(`${ruta}\n    ${detalle}`);

console.log(`Verificando ${base}${referencia ? `  (comparando con ${referencia})` : ''}\n`);

for (const ruta of [...paginas, ...SESION, ...ARCHIVOS, INEXISTENTE]) {
  const r = await pedir(base + ruta);

  if (paginas.includes(ruta)) {
    // Una pagina prerenderizada tiene que traer SU canonical. Si no hay
    // canonical, lo que llego es el cascaron del SPA: responde 200 y el
    // navegador acaba pintando bien, pero Google y las tarjetas de
    // WhatsApp/LinkedIn solo leen esto.
    if (r.estado !== 200) {
      anotar(ruta, `esperaba 200 y respondio ${resumen(r)}`);
    } else if (!r.canonical) {
      anotar(ruta, `sirvio el cascaron del SPA (sin canonical) — titulo: ${r.titulo}`);
    } else if (new URL(r.canonical).pathname.replace(/\/$/, '') !== ruta.replace(/\/$/, '')) {
      anotar(ruta, `canonical de otra pagina: ${r.canonical}`);
    }
  } else if (SESION.includes(ruta)) {
    // Al reves: aqui SI debe llegar el cascaron, y nunca la Home.
    if (r.estado !== 200) {
      anotar(ruta, `esperaba 200 (cascaron del SPA) y respondio ${resumen(r)}`);
    } else if (r.canonical) {
      anotar(ruta, `sirvio una pagina prerenderizada en vez del cascaron: ${r.canonical}`);
    }
  } else if (ARCHIVOS.includes(ruta)) {
    if (r.estado !== 200) {
      anotar(ruta, `esperaba 200 y respondio ${resumen(r)}`);
    }
  } else if (r.estado !== 404) {
    anotar(ruta, `una ruta inexistente debe responder 404, no ${resumen(r)}`);
  }

  if (referencia) {
    const ref = await pedir(referencia + ruta);
    const distinto = r.estado !== ref.estado || r.canonical !== ref.canonical;
    console.log(
      `${distinto ? 'DISTINTO' : '   igual'}  ${ruta.padEnd(38)} ${resumen(r)}` +
        (distinto ? `\n${' '.repeat(50)}referencia: ${resumen(ref)}` : ''),
    );
  }
}

if (fallos.length) {
  console.error(`\n${fallos.length} fallo(s) en ${base}:\n`);
  fallos.forEach((fallo, i) => console.error(`  ${i + 1}. ${fallo}`));
  console.error('');
  process.exit(1);
}

console.log(
  `\nOK: ${paginas.length} paginas prerenderizadas con su canonical, ` +
    `${SESION.length} rutas de sesion con el cascaron, ${ARCHIVOS.length} archivos y 404 real.`,
);
