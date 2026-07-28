import { describe, expect, it } from 'vitest';

import { ARTICULOS } from '../contenido/blog';
import { CASOS } from '../contenido/casos';
import { SERVICIOS } from '../contenido/servicios';
import { BASE_URL } from '../contenido/sitio';
import { generarSitemap } from './sitemap';

describe('generarSitemap', () => {
  it('es un XML valido con la cabecera y el elemento urlset', () => {
    const xml = generarSitemap();

    expect(xml.startsWith('<?xml version="1.0" encoding="UTF-8"?>')).toBe(true);
    expect(xml).toContain('<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">');
    expect(xml).toContain('</urlset>');
  });

  it('incluye las rutas publicas estaticas con la URL base', () => {
    const xml = generarSitemap();

    expect(xml).toContain(`<loc>${BASE_URL}/</loc>`);
    expect(xml).toContain(`<loc>${BASE_URL}/casos</loc>`);
    expect(xml).toContain(`<loc>${BASE_URL}/sobre-nosotros</loc>`);
    expect(xml).toContain(`<loc>${BASE_URL}/blog</loc>`);
    expect(xml).toContain(`<loc>${BASE_URL}/contacto</loc>`);
    expect(xml).toContain(`<loc>${BASE_URL}/legales/politica-de-datos</loc>`);
    expect(xml).toContain(`<loc>${BASE_URL}/legales/terminos</loc>`);
  });

  it('incluye una entrada por cada servicio, caso y articulo de blog', () => {
    const xml = generarSitemap();

    for (const servicio of SERVICIOS) {
      expect(xml).toContain(`<loc>${BASE_URL}/servicios/${servicio.slug}</loc>`);
    }
    for (const caso of CASOS) {
      expect(xml).toContain(`<loc>${BASE_URL}/casos/${caso.slug}</loc>`);
    }
    for (const articulo of ARTICULOS) {
      expect(xml).toContain(`<loc>${BASE_URL}/blog/${articulo.slug}</loc>`);
    }
  });

  it('no incluye ninguna ruta del panel admin', () => {
    const xml = generarSitemap();

    expect(xml).not.toContain('/admin');
  });

  it('de las paginas de cuenta solo incluye /registro', () => {
    const xml = generarSitemap();

    expect(xml).toContain(`<loc>${BASE_URL}/registro</loc>`);
    expect(xml).not.toContain('/mi-cuenta');
    expect(xml).not.toContain('/verificar-correo');
    expect(xml).not.toContain('/restablecer-contrasena');
    expect(xml).not.toContain('/ingreso');
    expect(xml).not.toContain('/recuperar-contrasena');
  });
});
