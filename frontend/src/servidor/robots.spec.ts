import { describe, expect, it } from 'vitest';

import { BASE_URL } from '../contenido/sitio';
import { generarRobotsTxt } from './robots';

describe('generarRobotsTxt', () => {
  it('permite el rastreo general y excluye el panel admin', () => {
    const txt = generarRobotsTxt();

    expect(txt).toContain('User-agent: *');
    expect(txt).toContain('Disallow: /admin');
    expect(txt).toContain('Allow: /');
  });

  it('excluye las paginas privadas o de un solo uso de la cuenta de cliente', () => {
    const txt = generarRobotsTxt();

    expect(txt).toContain('Disallow: /mi-cuenta');
    expect(txt).toContain('Disallow: /verificar-correo');
    expect(txt).toContain('Disallow: /restablecer-contrasena');
  });

  it('referencia el sitemap con la URL base configurada, sin dominio hardcodeado', () => {
    const txt = generarRobotsTxt();

    expect(txt).toContain(`Sitemap: ${BASE_URL}/sitemap.xml`);
  });
});
