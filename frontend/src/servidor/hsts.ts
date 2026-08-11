import type { NextFunction, Request, Response } from 'express';

/**
 * HSTS emitido por la propia aplicación (ADR-11, revisado el 11 ago
 * 2026). Arranque conservador: 1 día. El valor sale de `HSTS_MAX_AGE`
 * para poder subirlo desde el dashboard, sin tocar código.
 *
 * Sin `preload` a propósito: entrar a la lista de precarga de los
 * navegadores es, en la práctica, irreversible (salir tarda meses en
 * propagarse). Solo se añade cuando el `max-age` lleve tiempo en
 * 31536000 y el dominio esté estable.
 */
const MAX_AGE_POR_DEFECTO = '86400';

export function valorDeHsts(maxAge: string | undefined = process.env['HSTS_MAX_AGE']): string {
  const segundos = maxAge?.trim() ? maxAge.trim() : MAX_AGE_POR_DEFECTO;
  return `max-age=${segundos}; includeSubDomains`;
}

/**
 * Detrás del proxy la petición original llega en `x-forwarded-proto`.
 * Puede traer varios saltos separados por coma (Cloudflare → Render →
 * app): el primero es el del cliente, que es el que importa.
 */
export function esPeticionHttps(cabecera: string | string[] | undefined): boolean {
  const valor = Array.isArray(cabecera) ? cabecera[0] : cabecera;
  return (valor ?? '').split(',')[0]?.trim().toLowerCase() === 'https';
}

export function cabeceraHsts() {
  return (req: Request, res: Response, next: NextFunction): void => {
    // En local se sirve por http: emitir HSTS ahí dejaría el navegador
    // forzando https contra localhost, rompiendo el desarrollo.
    if (esPeticionHttps(req.headers['x-forwarded-proto'])) {
      res.setHeader('Strict-Transport-Security', valorDeHsts());
    }
    next();
  };
}
