import { PALETAS, Paleta } from './paletas';

function luminancia(hex: string): number {
  const [r, g, b] = [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16) / 255);
  const lineal = (c: number) => (c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4);
  return 0.2126 * lineal(r) + 0.7152 * lineal(g) + 0.0722 * lineal(b);
}

function contraste(a: string, b: string): number {
  const [claro, oscuro] = [luminancia(a), luminancia(b)].sort((x, y) => y - x);
  return (claro + 0.05) / (oscuro + 0.05);
}

/** Los pares de texto sobre fondo que de verdad dibuja el boceto. */
const PARES: [keyof Paleta, keyof Paleta][] = [
  ['tinta', 'pantalla'],
  ['tinta', 'superficie'],
  ['tinta', 'tinte'],
  ['suave', 'pantalla'],
  ['suave', 'superficie'],
  ['sobreAcento', 'acento'],
  ['bannerTexto', 'banner'],
];

describe('Paletas del boceto (ISS-238, decisión 35)', () => {
  it('son ocho, una por cada valor que puede elegir la IA', () => {
    expect(Object.keys(PALETAS).sort()).toEqual(
      ['CALIDA', 'ELEGANTE', 'FRESCA', 'INDUSTRIAL', 'NATURAL', 'NOCTURNA', 'OCEANO', 'SALUD'],
    );
  });

  // El sitio entero cumple AA; las apps que dibuja para el visitante también.
  for (const [nombre, paleta] of Object.entries(PALETAS)) {
    it(`${nombre} cumple contraste AA en cada par de texto`, () => {
      for (const [texto, fondo] of PARES) {
        expect(contraste(paleta[texto] as string, paleta[fondo] as string), `${texto} sobre ${fondo}`).toBeGreaterThanOrEqual(4.5);
      }
    });
  }
});
