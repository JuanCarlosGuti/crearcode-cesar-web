import { BASE_URL } from '../contenido/sitio';

export function generarRobotsTxt(): string {
  return `User-agent: *\nDisallow: /admin\nAllow: /\n\nSitemap: ${BASE_URL}/sitemap.xml\n`;
}
