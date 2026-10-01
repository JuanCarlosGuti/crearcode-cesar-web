import { formatearFecha, formatearFechaDeInstante, formatearPesos } from './formato';

describe('formato', () => {
  it('formatea pesos colombianos sin decimales', () => {
    expect(formatearPesos(1190000)).toMatch(/^\$\s?1\.190\.000$/);
    expect(formatearPesos(0)).toMatch(/^\$\s?0$/);
  });

  /**
   * Una fecha sin hora ("2026-09-01") no puede interpretarse en UTC: en
   * Bogotá (UTC-5) eso es el 31 de agosto a las 7 p. m., y el portal le
   * mostraría al cliente un día antes del que se acordó.
   */
  it('una fecha sin hora se muestra el mismo día, en cualquier zona', () => {
    expect(formatearFecha('2026-09-01')).toBe('1 de septiembre de 2026');
    expect(formatearFecha('2026-12-31')).toBe('31 de diciembre de 2026');
  });

  it('un instante se muestra en la hora de Colombia', () => {
    // 2 a. m. del 1 de octubre en UTC es todavía 30 de septiembre en Bogotá.
    expect(formatearFechaDeInstante('2026-10-01T02:00:00Z')).toBe('30 de septiembre de 2026');
  });

  it('sin fecha no inventa una', () => {
    expect(formatearFecha(null)).toBe('');
    expect(formatearFechaDeInstante(null)).toBe('');
  });
});
