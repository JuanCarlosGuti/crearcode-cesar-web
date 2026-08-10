import { describe, expect, it } from 'vitest';

import { COTIZADOR } from './cotizador';

describe('COTIZADOR (datos del wizard, HU-39)', () => {
  it('tiene exactamente 3 pasos con clave, titulo, ayuda y al menos 2 opciones', () => {
    expect(COTIZADOR.pasos).toHaveLength(3);
    for (const paso of COTIZADOR.pasos) {
      expect(paso.clave.length).toBeGreaterThan(0);
      expect(paso.titulo.length).toBeGreaterThan(0);
      expect(paso.ayuda.length).toBeGreaterThan(0);
      expect(paso.opciones.length).toBeGreaterThanOrEqual(2);
    }
  });

  it('las claves de los pasos son unicas y en el orden tipo, alcance, urgencia', () => {
    expect(COTIZADOR.pasos.map((p) => p.clave)).toEqual(['tipo', 'alcance', 'urgencia']);
  });

  it('cada opcion de alcance tiene su rango orientativo definido', () => {
    const alcance = COTIZADOR.pasos.find((p) => p.clave === 'alcance')!;
    for (const opcion of alcance.opciones) {
      const rango = COTIZADOR.rangosPorAlcance[opcion];
      expect(rango, `falta rango para "${opcion}"`).toBeTruthy();
      expect(rango).toContain('COP');
    }
  });

  it('el resultado aclara que se cotiza a la medida (regla de honestidad)', () => {
    expect(COTIZADOR.resultado.aclaracion).toContain('a la medida');
  });
});
