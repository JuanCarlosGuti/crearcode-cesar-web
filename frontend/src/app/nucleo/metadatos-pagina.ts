import { DOCUMENT } from '@angular/common';
import { effect, inject } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';

import { BASE_URL, IMAGEN_OG_DEFECTO } from '../../contenido/sitio';

export interface MetadatosDePagina {
  titulo: string;
  descripcion: string;
  /** Ruta relativa de la página (ej. `/servicios/ia-y-automatizacion`), para construir `og:url`. */
  ruta: string;
  /** Ruta relativa de una imagen Open Graph propia; si no se da, se usa {@link IMAGEN_OG_DEFECTO}. */
  imagen?: string;
  /**
   * Tipo Open Graph. Por defecto `website`; los articulos del blog
   * declaran `article` — se anunciaban como sitio, y con eso pierden
   * la fecha y el autor en la tarjeta (auditoria del 28 sep 2026).
   */
  tipo?: 'website' | 'article';
  /** Fecha ISO de publicacion; solo tiene sentido con `tipo: 'article'`. */
  publicadoEn?: string;
}

/**
 * Establece `<title>`, meta description y Open Graph de la página actual
 * (HU-23, HU-24). Reactivo: se puede llamar con datos que todavía no
 * cargaron (páginas por slug mientras se resuelve el contenido) — no
 * hace nada hasta que `datos()` deja de ser `undefined`.
 */
export function establecerMetadatosDePagina(datos: () => MetadatosDePagina | undefined): void {
  const title = inject(Title);
  const meta = inject(Meta);
  const documento = inject(DOCUMENT);

  effect(() => {
    const valores = datos();
    if (!valores) {
      return;
    }

    title.setTitle(valores.titulo);
    meta.updateTag({ name: 'description', content: valores.descripcion });
    meta.updateTag({ property: 'og:type', content: valores.tipo ?? 'website' });
    meta.updateTag({ property: 'og:title', content: valores.titulo });
    meta.updateTag({ property: 'og:description', content: valores.descripcion });
    meta.updateTag({ property: 'og:url', content: `${BASE_URL}${valores.ruta}` });
    const imagen = `${BASE_URL}${valores.imagen ?? IMAGEN_OG_DEFECTO}`;
    meta.updateTag({ property: 'og:image', content: imagen });
    // Idioma, nombre del sitio y tarjeta de Twitter: sin ellos WhatsApp
    // y LinkedIn arman la previsualizacion con lo que adivinan
    // (auditoria SEO del 28 sep 2026).
    meta.updateTag({ property: 'og:locale', content: 'es_CO' });
    meta.updateTag({ property: 'og:site_name', content: 'Crear Code Cesar' });
    meta.updateTag({ name: 'twitter:card', content: 'summary_large_image' });
    meta.updateTag({ name: 'twitter:title', content: valores.titulo });
    meta.updateTag({ name: 'twitter:description', content: valores.descripcion });
    meta.updateTag({ name: 'twitter:image', content: imagen });
    if (valores.tipo === 'article' && valores.publicadoEn) {
      meta.updateTag({ property: 'article:published_time', content: valores.publicadoEn });
      meta.updateTag({ property: 'article:author', content: 'Crear Code Cesar S.A.S.' });
    }

    // Canonical (ADR-11): Meta de Angular solo maneja <meta>, así que el
    // <link rel="canonical"> se crea/actualiza directo en el documento.
    let canonical = documento.head.querySelector<HTMLLinkElement>('link[rel="canonical"]');
    if (!canonical) {
      canonical = documento.createElement('link');
      canonical.setAttribute('rel', 'canonical');
      documento.head.appendChild(canonical);
    }
    canonical.setAttribute('href', `${BASE_URL}${valores.ruta}`);
  });
}
