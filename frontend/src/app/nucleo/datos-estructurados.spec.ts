import { DOCUMENT } from '@angular/common';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { BASE_URL } from '../../contenido/sitio';
import {
  establecerDatosEstructuradosDeLaEmpresa,
  establecerDatosEstructuradosDePagina,
} from './datos-estructurados';

@Component({ template: '' })
class AnfitrionDePrueba {
  constructor() {
    establecerDatosEstructuradosDeLaEmpresa();
  }
}

/**
 * JSON-LD de la empresa (schema.org ProfessionalService) para el SEO
 * local: sin datos estructurados Google no tiene NAP, fundador ni area
 * de servicio que mostrar (auditoria del 28 sep 2026, seccion 7).
 * Se genera desde las constantes del sitio, no como HTML fijo, para no
 * duplicar el dominio (ADR-06).
 */
describe('establecerDatosEstructuradosDeLaEmpresa', () => {
  function leerJsonLd(): Record<string, unknown> {
    const script = TestBed.inject(DOCUMENT).head.querySelector('script[type="application/ld+json"]');
    expect(script, 'debe existir el script ld+json en <head>').toBeTruthy();
    return JSON.parse(script!.textContent ?? '{}');
  }

  afterEach(() => {
    TestBed.inject(DOCUMENT)
      .head.querySelectorAll('script[type="application/ld+json"]')
      .forEach((script) => script.remove());
  });

  it('publica un ProfessionalService con NAP, fundador y area de servicio, con el dominio canonico', async () => {
    const fixture = TestBed.createComponent(AnfitrionDePrueba);
    await fixture.whenStable();

    const datos = leerJsonLd();
    expect(datos['@context']).toBe('https://schema.org');
    expect(datos['@type']).toBe('ProfessionalService');
    expect(datos['url']).toBe(BASE_URL);
    expect(datos['legalName']).toBe('Crear Code Cesar S.A.S.');
    expect(datos['taxID']).toBe('901941017-0');
    expect(datos['telephone']).toBe('+573239885883');
    const direccion = datos['address'] as Record<string, string>;
    expect(direccion['streetAddress']).toContain('Calle 4B # 20-36');
    expect(direccion['addressLocality']).toBe('Valledupar');
    expect(direccion['addressCountry']).toBe('CO');
    expect(datos['areaServed']).toContain('Valledupar');
    const fundador = datos['founder'] as Record<string, string>;
    expect(fundador['name']).toBe('Juan Carlos Gutiérrez');
    expect(fundador['sameAs']).toContain('linkedin.com');
  });

  it('no duplica el script si se llama mas de una vez', async () => {
    const fixture = TestBed.createComponent(AnfitrionDePrueba);
    await fixture.whenStable();
    TestBed.runInInjectionContext(() => establecerDatosEstructuradosDeLaEmpresa());

    const scripts = TestBed.inject(DOCUMENT).head.querySelectorAll('script[type="application/ld+json"]');
    expect(scripts).toHaveLength(1);
  });
});

/**
 * Article y BreadcrumbList: no existia ninguno de los dos (auditoria
 * del 28 sep 2026, P1-6b). La miga importa aunque la pagina ya la
 * muestre: es lo que hace que Google pinte "Inicio > Blog > Articulo"
 * en el resultado en vez de la URL cruda.
 */
describe('establecerDatosEstructuradosDePagina', () => {
  @Component({ template: '' })
  class SoloMigas {
    constructor() {
      establecerDatosEstructuradosDePagina(() => ({
        migas: [
          { nombre: 'Inicio', ruta: '/' },
          { nombre: 'Blog', ruta: '/blog' },
        ],
      }));
    }
  }

  @Component({ template: '' })
  class ArticuloCompleto {
    constructor() {
      establecerDatosEstructuradosDePagina(() => ({
        migas: [
          { nombre: 'Inicio', ruta: '/' },
          { nombre: 'Blog', ruta: '/blog' },
        ],
        articulo: {
          titulo: 'Señales de que toca automatizar',
          resumen: 'Cuándo deja de compensar hacerlo a mano.',
          fecha: '2026-06-15',
          ruta: '/blog/senales',
        },
      }));
    }
  }

  function leerGrafo(): Record<string, unknown>[] {
    const script = TestBed.inject(DOCUMENT).head.querySelector('#datos-estructurados-pagina');
    expect(script, 'debe existir el ld+json de la pagina').toBeTruthy();
    const contenido = JSON.parse(script!.textContent ?? '{}');
    return Array.isArray(contenido) ? contenido : [contenido];
  }

  afterEach(() => {
    TestBed.inject(DOCUMENT).head.querySelector('#datos-estructurados-pagina')?.remove();
  });

  it('publica la miga de pan con posiciones y URLs absolutas', async () => {
    const fixture = TestBed.createComponent(SoloMigas);
    await fixture.whenStable();

    const miga = leerGrafo().find((n) => n['@type'] === 'BreadcrumbList')!;
    expect(miga['itemListElement']).toEqual([
      { '@type': 'ListItem', position: 1, name: 'Inicio', item: `${BASE_URL}/` },
      { '@type': 'ListItem', position: 2, name: 'Blog', item: `${BASE_URL}/blog` },
    ]);
  });

  it('un articulo publica ademas su Article con fecha y autor', async () => {
    const fixture = TestBed.createComponent(ArticuloCompleto);
    await fixture.whenStable();

    const articulo = leerGrafo().find((n) => n['@type'] === 'Article')!;
    expect(articulo['headline']).toBe('Señales de que toca automatizar');
    expect(articulo['datePublished']).toBe('2026-06-15');
    expect(articulo['mainEntityOfPage']).toBe(`${BASE_URL}/blog/senales`);
    expect((articulo['author'] as Record<string, string>)['name']).toContain('Crear Code Cesar');
  });

  /**
   * En una SPA se navega de un articulo a otro sin recargar el
   * documento: dos Article en el mismo <head> describirian una pagina
   * que no existe.
   */
  it('al cambiar de pagina reemplaza el bloque en vez de acumularlo', async () => {
    const primera = TestBed.createComponent(ArticuloCompleto);
    await primera.whenStable();
    const segunda = TestBed.createComponent(SoloMigas);
    await segunda.whenStable();

    expect(
      TestBed.inject(DOCUMENT).head.querySelectorAll('#datos-estructurados-pagina'),
    ).toHaveLength(1);
    expect(leerGrafo().some((n) => n['@type'] === 'Article')).toBe(false);
  });
});
