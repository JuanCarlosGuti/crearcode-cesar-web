/**
 * Dominio canónico del sitio (ADR-06): el único lugar donde vive. Nada
 * más en el frontend debe escribir el dominio a mano — las metas, el
 * canonical, el sitemap y el JSON-LD salen todos de aquí.
 *
 * Es una constante y no una variable de entorno porque el sitio se
 * prerenderiza: para cuando algo podría leer el entorno, el HTML ya
 * está escrito.
 *
 * Comprado el 10 ago 2026 (ADR-11). Sin `www`: el `www` redirige 301
 * desde el Caddyfile, no desde Cloudflare — Cloudflare es solo el DNS
 * desde el corte al servidor propio (ADR-13).
 */
export const BASE_URL = 'https://crearcodecesar.com';

export const IMAGEN_OG_DEFECTO = '/imagenes/og-defecto.jpg';
