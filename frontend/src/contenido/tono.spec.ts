import { COTIZADOR } from './cotizador';
import { DEMO_DISENO } from './demo-diseno';
import { DIAGNOSTICO } from './diagnostico';
import { HERRAMIENTAS } from './herramientas';
import { HOME } from './home';
import { SIMULADOR } from './simulador';

/** Todos los textos de un contenido, recorriéndolo entero. */
function textos(valor: unknown): string[] {
  if (typeof valor === 'string') return [valor];
  if (Array.isArray(valor)) return valor.flatMap(textos);
  if (valor && typeof valor === 'object') return Object.values(valor).flatMap(textos);
  return [];
}

/**
 * El sitio le habla al visitante de tú (docs/07, tono). El diagnóstico
 * preguntaba «¿Cómo reciben hoy los pedidos de sus clientes?» en medio
 * de todo lo demás en tú (hipótesis H-07 de la auditoría de diseño).
 * «Ustedes» sí vale: es el visitante hablándole a la empresa.
 */
describe('Tono de las herramientas y la Home', () => {
  const contenidos = { COTIZADOR, DEMO_DISENO, DIAGNOSTICO, HERRAMIENTAS, HOME, SIMULADOR };

  for (const [nombre, contenido] of Object.entries(contenidos)) {
    it(`${nombre} le habla al visitante de tú`, () => {
      const deUsted = textos(contenido).filter((t) => /\busted\b|\bsus clientes\b|\bsu (negocio|empresa)\b|\breciben\b/i.test(t));
      expect(deUsted).toEqual([]);
    });
  }
});
