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

  /**
   * Las 12 combinaciones de tipo x alcance, una por una: antes el
   * rango dependia solo del alcance y las 36 combinaciones del wizard
   * daban 3 resultados (auditoria P1-2a). Si manana se anade un tipo
   * de proyecto, este test lo pide con sus cifras.
   */
  it('cada combinacion de tipo y alcance tiene su rango orientativo', () => {
    const tipos = COTIZADOR.pasos.find((p) => p.clave === 'tipo')!.opciones;
    const alcances = COTIZADOR.pasos.find((p) => p.clave === 'alcance')!.opciones;

    for (const tipo of tipos) {
      for (const alcance of alcances) {
        const rango = COTIZADOR.rangosPorTipoYAlcance[tipo]?.[alcance];
        expect(rango, `falta rango para "${tipo}" / "${alcance}"`).toBeTruthy();
        expect(rango).toContain('COP');
      }
    }
  });

  /**
   * Una pagina web no puede arrancar en el mismo piso que un sistema
   * a la medida: era lo que espantaba al cliente mas facil de atender.
   */
  it('una pagina web arranca por debajo de un sistema a la medida', () => {
    const primerNumero = (rango: string) => Number(rango.replace('COP ', '').split(' ')[0].replace(',', '.'));
    const web = COTIZADOR.rangosPorTipoYAlcance['Página web o tienda en línea'];
    const sistema = COTIZADOR.rangosPorTipoYAlcance['Sistema interno a la medida'];

    expect(primerNumero(web['Algo puntual, una sola función'])).toBeLessThan(
      primerNumero(sistema['Algo puntual, una sola función']),
    );
  });

  it('cada opcion de urgencia tiene su nota, aunque sea vacia', () => {
    const urgencia = COTIZADOR.pasos.find((p) => p.clave === 'urgencia')!;
    for (const opcion of urgencia.opciones) {
      expect(COTIZADOR.notaPorUrgencia[opcion], `falta nota para "${opcion}"`).toBeDefined();
    }
  });

  it('el resultado aclara que se cotiza a la medida (regla de honestidad)', () => {
    expect(COTIZADOR.resultado.aclaracion).toContain('a la medida');
  });
});
