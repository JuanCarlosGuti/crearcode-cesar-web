import { ARTICULOS } from '../contenido/blog';
import { CASOS } from '../contenido/casos';
import { SERVICIOS } from '../contenido/servicios';
import { BASE_URL } from '../contenido/sitio';

// De las paginas de cuenta (F8) solo /registro entra al sitemap: es la
// unica con valor de captacion. Ingreso/recuperacion no aportan SEO y
// las de token/area privada quedan ademas excluidas en robots.txt.
const RUTAS_ESTATICAS: readonly string[] = [
  '/',
  '/herramientas',
  '/casos',
  '/sobre-nosotros',
  '/blog',
  '/contacto',
  '/registro',
  '/legales/politica-de-datos',
  '/legales/terminos',
];

interface EntradaDeSitemap {
  readonly ruta: string;
  /** Fecha ISO (AAAA-MM-DD) o `undefined` si la pagina no tiene una fiable. */
  readonly ultimaModificacion?: string;
}

/**
 * `lastmod` solo donde hay una fecha de verdad: los articulos del blog,
 * que la traen en el contenido. Las paginas estaticas, los servicios y
 * los proyectos no tienen ningun campo de fecha, y poner la del build
 * seria decirle a Google que todo el sitio cambia en cada despliegue —
 * una senal falsa se ignora, y con ella se ignoran tambien las
 * verdaderas (auditoria del 28 sep 2026, P1-6d).
 */
export function generarSitemap(): string {
  const entradas: readonly EntradaDeSitemap[] = [
    ...RUTAS_ESTATICAS.map((ruta) => ({ ruta })),
    ...SERVICIOS.map((servicio) => ({ ruta: `/servicios/${servicio.slug}` })),
    ...CASOS.map((caso) => ({ ruta: `/casos/${caso.slug}` })),
    ...ARTICULOS.map((articulo) => ({
      ruta: `/blog/${articulo.slug}`,
      ultimaModificacion: articulo.fecha,
    })),
  ];

  const urls = entradas
    .map(({ ruta, ultimaModificacion }) => {
      const lastmod = ultimaModificacion ? `\n    <lastmod>${ultimaModificacion}</lastmod>` : '';
      return `  <url>\n    <loc>${BASE_URL}${ruta}</loc>${lastmod}\n  </url>`;
    })
    .join('\n');

  return `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${urls}\n</urlset>\n`;
}
