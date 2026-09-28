import { DOCUMENT } from '@angular/common';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { BASE_URL } from '../../contenido/sitio';
import { establecerDatosEstructuradosDeLaEmpresa } from './datos-estructurados';

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
