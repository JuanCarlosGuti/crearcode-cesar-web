import { DOCUMENT } from '@angular/common';
import { effect, inject } from '@angular/core';

import { EMPRESA } from '../../contenido/empresa';
import { BASE_URL, IMAGEN_OG_DEFECTO } from '../../contenido/sitio';
import { SOBRE_NOSOTROS } from '../../contenido/sobre-nosotros';

const ID_SCRIPT = 'datos-estructurados-empresa';
const ID_SCRIPT_PAGINA = 'datos-estructurados-pagina';

export interface MigaDePan {
  readonly nombre: string;
  readonly ruta: string;
}

export interface DatosDeArticulo {
  readonly titulo: string;
  readonly resumen: string;
  /** Fecha ISO de publicacion (AAAA-MM-DD). */
  readonly fecha: string;
  readonly ruta: string;
}

export interface DatosDePagina {
  readonly migas: readonly MigaDePan[];
  readonly articulo?: DatosDeArticulo;
}

/**
 * Inyecta en <head> el JSON-LD de la empresa (schema.org
 * ProfessionalService): nombre, NIT, direccion, telefono, fundador y
 * area de servicio. Es lo que Google usa para el resultado local y lo
 * que faltaba por completo (auditoria del 28 sep 2026, seccion 7).
 *
 * Se arma desde las constantes del sitio y no como HTML fijo en
 * index.html, para que el dominio siga viviendo en un solo lugar
 * (ADR-06) y los datos de contacto en otro (contenido/empresa.ts).
 * Idempotente: el prerender y la hidratacion pueden llamarlo dos veces.
 */
export function establecerDatosEstructuradosDeLaEmpresa(): void {
  const documento = inject(DOCUMENT);
  if (documento.getElementById(ID_SCRIPT)) {
    return;
  }

  const datos = {
    '@context': 'https://schema.org',
    '@type': 'ProfessionalService',
    '@id': `${BASE_URL}/#empresa`,
    name: 'Crear Code Cesar',
    legalName: EMPRESA.razonSocial,
    taxID: EMPRESA.nit,
    url: BASE_URL,
    logo: `${BASE_URL}/apple-touch-icon.png`,
    image: `${BASE_URL}${IMAGEN_OG_DEFECTO}`,
    telephone: `+${EMPRESA.whatsappNumeroInternacional}`,
    email: EMPRESA.correo,
    address: {
      '@type': 'PostalAddress',
      streetAddress: EMPRESA.direccion,
      addressLocality: 'Valledupar',
      addressRegion: 'Cesar',
      addressCountry: 'CO',
    },
    areaServed: ['Valledupar', 'Cesar', 'La Guajira', 'Magdalena', 'Colombia'],
    founder: {
      '@type': 'Person',
      name: SOBRE_NOSOTROS.fundador.nombre,
      sameAs: SOBRE_NOSOTROS.fundador.linkedinUrl,
    },
    sameAs: [SOBRE_NOSOTROS.fundador.linkedinUrl, 'https://github.com/JuanCarlosGuti'],
    knowsAbout: [
      'Desarrollo de software a la medida',
      'Inteligencia artificial y automatización para pymes',
      'Cobro digital e integraciones',
    ],
  };

  const script = documento.createElement('script');
  script.id = ID_SCRIPT;
  script.type = 'application/ld+json';
  script.textContent = JSON.stringify(datos);
  documento.head.appendChild(script);
}

/**
 * Inyecta el JSON-LD propio de una pagina de detalle: la miga de pan
 * (BreadcrumbList) y, en los articulos del blog, el Article con su
 * fecha y su autor.
 *
 * <p>
 * No existia ninguno (auditoria del 28 sep 2026, P1-6b). La miga
 * importa aunque la pagina ya la muestre visualmente: es lo que hace
 * que Google pinte "Inicio > Blog > Articulo" en vez de la URL cruda,
 * y lo que le dice donde encaja cada pagina dentro del sitio.
 *
 * <p>
 * Reemplaza el script anterior en vez de acumular: en una SPA se
 * navega de un articulo a otro sin recargar el documento, y dos
 * Article en el mismo <head> describirian una pagina que no existe.
 */
export function establecerDatosEstructuradosDePagina(datos: () => DatosDePagina | undefined): void {
  const documento = inject(DOCUMENT);

  // Reactivo como establecerMetadatosDePagina, y por lo mismo: las
  // paginas de detalle resuelven su contenido desde un input required
  // que todavia no existe cuando corre el constructor.
  effect(() => {
    const valores = datos();
    if (!valores) {
      return;
    }
    pintar(documento, valores);
  });
}

function pintar(documento: Document, { migas, articulo }: DatosDePagina): void {
  const grafo: unknown[] = [];

  if (migas.length > 0) {
    grafo.push({
      '@context': 'https://schema.org',
      '@type': 'BreadcrumbList',
      itemListElement: migas.map((miga, posicion) => ({
        '@type': 'ListItem',
        position: posicion + 1,
        name: miga.nombre,
        item: `${BASE_URL}${miga.ruta}`,
      })),
    });
  }

  if (articulo) {
    grafo.push({
      '@context': 'https://schema.org',
      '@type': 'Article',
      headline: articulo.titulo,
      description: articulo.resumen,
      datePublished: articulo.fecha,
      mainEntityOfPage: `${BASE_URL}${articulo.ruta}`,
      image: `${BASE_URL}${IMAGEN_OG_DEFECTO}`,
      author: { '@type': 'Organization', name: EMPRESA.razonSocial, url: BASE_URL },
      publisher: { '@type': 'Organization', name: EMPRESA.razonSocial, url: BASE_URL },
    });
  }

  documento.getElementById(ID_SCRIPT_PAGINA)?.remove();
  if (grafo.length === 0) {
    return;
  }
  const script = documento.createElement('script');
  script.id = ID_SCRIPT_PAGINA;
  script.type = 'application/ld+json';
  script.textContent = JSON.stringify(grafo.length === 1 ? grafo[0] : grafo);
  documento.head.appendChild(script);
}
