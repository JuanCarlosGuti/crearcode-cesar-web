/**
 * `baseUrl` pendiente del dominio definitivo (ver docs/01-vision-y-alcance.md
 * §7 y ADR-06 en docs/02-arquitectura.md): placeholder con el TLD
 * `.example`, reservado por IANA específicamente para documentación/
 * pruebas, hasta que se compre el dominio real en la fase F7. Actualizar
 * este único valor ese día — nada más en el sitio debe hardcodear el
 * dominio (ADR-06).
 */
// Dominio canónico (ADR-11, comprado el 10 ago 2026): sin www; el www
// redirige 301 en Cloudflare. Constante y no variable de entorno
// porque las metas se hornean en el prerender.
export const BASE_URL = 'https://crearcodecesar.com';

export const IMAGEN_OG_DEFECTO = '/imagenes/og-defecto.jpg';
