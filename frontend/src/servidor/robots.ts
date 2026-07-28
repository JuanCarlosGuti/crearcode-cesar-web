import { BASE_URL } from '../contenido/sitio';

export function generarRobotsTxt(): string {
  return `User-agent: *\nDisallow: /admin\nDisallow: /mi-cuenta\nDisallow: /verificar-correo\nDisallow: /restablecer-contrasena\nAllow: /\n\nSitemap: ${BASE_URL}/sitemap.xml\n`;
}
