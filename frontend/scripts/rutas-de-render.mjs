// Motor de resolucion de rutas del Static Site de Render (ADR-12).
//
// Es la traduccion a codigo de dos frases de su documentacion, que son
// las que gobiernan que ve un visitante:
//
//   1. "Render does not apply redirect or rewrite rules to a path if a
//      resource exists at that path. Instead, Render simply serves the
//      resource at that path."
//   2. Las reglas se evaluan en orden y gana la primera que coincide.
//
// El matiz que costo un despliegue: /contacto NO es "un recurso en esa
// ruta" — el recurso es /contacto/index.html. Render solo resuelve el
// index de una carpeta cuando la URL trae barra final. Sin barra, la
// peticion cae en las reglas, asi que cada pagina prerenderizada
// necesita su regla explicita o el visitante recibe el cascaron del SPA.
//
// Se lee del render.yaml real para que el servidor local, la
// verificacion de CI y produccion no puedan divergir.

import { readFileSync } from 'node:fs';
import { join } from 'node:path';

const RENDER_YAML = join(import.meta.dirname, '..', '..', 'render.yaml');

/** Extrae las reglas del bloque `routes:` del render.yaml. */
export function leerReglas(archivo = RENDER_YAML) {
  const lineas = readFileSync(archivo, 'utf8').split(/\r?\n/);
  const inicio = lineas.findIndex((linea) => /^\s+routes:\s*$/.test(linea));
  if (inicio === -1) {
    throw new Error(`No hay bloque "routes:" en ${archivo}`);
  }
  const sangria = lineas[inicio].search(/\S/);

  const reglas = [];
  for (const linea of lineas.slice(inicio + 1)) {
    const contenido = linea.replace(/\s+#.*$/, '');
    if (!contenido.trim() || /^\s*#/.test(contenido)) continue;
    // Fin del bloque: cualquier clave hermana (headers:, envVars:, ...)
    if (contenido.search(/\S/) <= sangria) break;

    const [, clave, valor] = /^\s*(?:- )?([\w-]+):\s*(.+?)\s*$/.exec(contenido) ?? [];
    if (!clave) continue;
    if (/^\s*- /.test(contenido)) reglas.push({});
    reglas[reglas.length - 1][clave] = valor;
  }
  return reglas;
}

function aExpresion(patron) {
  const cuerpo = patron
    .replace(/[.+^${}()|[\]\\]/g, '\\$&')
    .replace(/:[\w-]+/g, '([^/]+)')
    .replace(/\*/g, '(.*)');
  return new RegExp(`^${cuerpo}$`);
}

function aplicar(regla, ruta) {
  const coincidencia = aExpresion(regla.source).exec(ruta);
  if (!coincidencia) return null;
  let indice = 0;
  return regla.destination.replace(/:[\w-]+|\*/g, () => coincidencia[++indice] ?? '');
}

/**
 * Devuelve lo que haria Render con `ruta`:
 *   { tipo: 'archivo', ruta }        — sirve un archivo del build
 *   { tipo: 'externo', destino }     — reenvia a otro host (el /api)
 *   { tipo: 'no-encontrado' }        — 404
 *
 * `existe` recibe una ruta relativa al directorio publicado y responde
 * si ahi hay un archivo (no una carpeta).
 */
export function resolver(ruta, existe, reglas) {
  if (existe(ruta)) {
    return { tipo: 'archivo', ruta };
  }
  // Solo con barra final Render resuelve el index de la carpeta.
  if (ruta.endsWith('/') && existe(`${ruta}index.html`)) {
    return { tipo: 'archivo', ruta: `${ruta}index.html` };
  }

  for (const regla of reglas) {
    const destino = aplicar(regla, ruta);
    if (destino === null) continue;
    if (/^https?:\/\//.test(destino)) {
      return { tipo: 'externo', destino };
    }
    return existe(destino) ? { tipo: 'archivo', ruta: destino } : { tipo: 'no-encontrado' };
  }
  return { tipo: 'no-encontrado' };
}
