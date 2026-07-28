import { ARTICULOS } from '../contenido/blog';
import { CASOS } from '../contenido/casos';
import { SERVICIOS } from '../contenido/servicios';
import { BASE_URL } from '../contenido/sitio';

// De las paginas de cuenta (F8) solo /registro entra al sitemap: es la
// unica con valor de captacion. Ingreso/recuperacion no aportan SEO y
// las de token/area privada quedan ademas excluidas en robots.txt.
const RUTAS_ESTATICAS: readonly string[] = [
  '/',
  '/casos',
  '/sobre-nosotros',
  '/blog',
  '/contacto',
  '/registro',
  '/legales/politica-de-datos',
  '/legales/terminos',
];

export function generarSitemap(): string {
  const rutas: readonly string[] = [
    ...RUTAS_ESTATICAS,
    ...SERVICIOS.map((servicio) => `/servicios/${servicio.slug}`),
    ...CASOS.map((caso) => `/casos/${caso.slug}`),
    ...ARTICULOS.map((articulo) => `/blog/${articulo.slug}`),
  ];

  const urls = rutas.map((ruta) => `  <url>\n    <loc>${BASE_URL}${ruta}</loc>\n  </url>`).join('\n');

  return `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${urls}\n</urlset>\n`;
}
