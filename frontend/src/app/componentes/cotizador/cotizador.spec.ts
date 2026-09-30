import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { COTIZADOR } from '../../../contenido/cotizador';
import { Cotizador } from './cotizador';

async function crear() {
  const fixture = TestBed.createComponent(Cotizador);
  await fixture.whenStable();
  return { fixture, el: fixture.nativeElement as HTMLElement };
}

async function elegir(fixture: Awaited<ReturnType<typeof crear>>['fixture'], texto: string) {
  const el = fixture.nativeElement as HTMLElement;
  const boton = Array.from(el.querySelectorAll<HTMLButtonElement>('.cotizador-opcion')).find((b) =>
    b.textContent?.includes(texto),
  );
  expect(boton, `no existe la opción "${texto}"`).toBeTruthy();
  boton!.click();
  await fixture.whenStable();
}

describe('Cotizador (wizard de 3 pasos, HU-39)', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('arranca en el paso 1 con su titulo y todas sus opciones', async () => {
    const { el } = await crear();
    expect(el.textContent).toContain(COTIZADOR.pasos[0].titulo);
    expect(el.querySelectorAll('.cotizador-opcion')).toHaveLength(COTIZADOR.pasos[0].opciones.length);
    expect(el.textContent).toContain('1 de 3');
  });

  it('elegir una opcion avanza al siguiente paso', async () => {
    const { fixture, el } = await crear();
    await elegir(fixture, 'Automatización con IA');
    expect(el.textContent).toContain(COTIZADOR.pasos[1].titulo);
    expect(el.textContent).toContain('2 de 3');
  });

  it('al completar los 3 pasos muestra el rango del tipo y alcance elegidos, y el resumen', async () => {
    const { fixture, el } = await crear();
    await elegir(fixture, 'Automatización con IA');
    await elegir(fixture, 'Varias funciones conectadas');
    await elegir(fixture, 'En 4 a 8 semanas');

    expect(el.textContent).toContain(
      COTIZADOR.rangosPorTipoYAlcance['Automatización con IA']['Varias funciones conectadas'],
    );
    expect(el.textContent).toContain('a la medida');
    const resumen = el.querySelector('.cotizador-resumen')!.textContent!;
    expect(resumen).toContain('Automatización con IA');
    expect(resumen).toContain('Varias funciones conectadas');
    expect(resumen).toContain('En 4 a 8 semanas');
  });

  /**
   * Antes el rango salia solo del alcance: las 36 combinaciones del
   * wizard daban 3 resultados y una pagina web arrancaba en el mismo
   * piso que un sistema a la medida (auditoria P1-2a).
   */
  it('cambiar solo el tipo de proyecto cambia el rango', async () => {
    const { fixture, el } = await crear();
    await elegir(fixture, 'Página web o tienda en línea');
    await elegir(fixture, 'Algo puntual, una sola función');
    await elegir(fixture, 'En 4 a 8 semanas');
    const conWeb = el.querySelector('.cotizador-rango')!.textContent;

    const segundo = await crear();
    await elegir(segundo.fixture, 'Sistema interno a la medida');
    await elegir(segundo.fixture, 'Algo puntual, una sola función');
    await elegir(segundo.fixture, 'En 4 a 8 semanas');

    expect(segundo.el.querySelector('.cotizador-rango')!.textContent).not.toBe(conWeb);
  });

  it('la urgencia deja su nota en el resultado', async () => {
    const { fixture, el } = await crear();
    await elegir(fixture, 'Automatización con IA');
    await elegir(fixture, 'Varias funciones conectadas');
    await elegir(fixture, 'Lo antes posible');

    expect(el.querySelector('.cotizador-nota-urgencia')!.textContent).toContain(
      COTIZADOR.notaPorUrgencia['Lo antes posible'],
    );
  });

  it('el resultado ofrece contacto y WhatsApp con la seleccion prellenada', async () => {
    const { fixture, el } = await crear();
    await elegir(fixture, 'Sistema interno a la medida');
    await elegir(fixture, 'Un sistema completo para el negocio');
    await elegir(fixture, 'Lo antes posible');

    expect(el.querySelector('a[href="/contacto"]')).toBeTruthy();
    const whatsapp = el.querySelector<HTMLAnchorElement>('a[href^="https://wa.me/"]')!;
    expect(whatsapp).toBeTruthy();
    expect(decodeURIComponent(whatsapp.href)).toContain('Sistema interno a la medida');
  });

  it('en el paso 1 no hay boton Atras', async () => {
    const { el } = await crear();
    expect(el.querySelector('.cotizador-atras')).toBeNull();
  });

  it('Atras vuelve al paso anterior sin perder lo ya elegido (auditoria QA4)', async () => {
    const { fixture, el } = await crear();
    await elegir(fixture, 'Automatización con IA');
    expect(el.textContent).toContain('2 de 3');

    (el.querySelector('.cotizador-atras') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(el.textContent).toContain(COTIZADOR.pasos[0].titulo);
    expect(el.textContent).toContain('1 de 3');

    // Cambiar de opinion en el paso 1 y seguir: el resumen refleja la
    // eleccion nueva, no la vieja.
    await elegir(fixture, 'Página web o tienda en línea');
    await elegir(fixture, 'Algo puntual, una sola función');
    await elegir(fixture, 'Sin afán, en los próximos meses');
    const resumen = el.querySelector('.cotizador-resumen')!.textContent!;
    expect(resumen).toContain('Página web o tienda en línea');
    expect(resumen).not.toContain('Automatización con IA');
  });

  it('desde el resultado, Atras vuelve al ultimo paso', async () => {
    const { fixture, el } = await crear();
    await elegir(fixture, 'Automatización con IA');
    await elegir(fixture, 'Varias funciones conectadas');
    await elegir(fixture, 'En 4 a 8 semanas');
    expect(el.textContent).toContain('COP');

    (el.querySelector('.cotizador-atras') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(el.textContent).toContain(COTIZADOR.pasos[2].titulo);
    expect(el.textContent).toContain('3 de 3');
    expect(el.textContent).not.toContain('COP');
  });

  it('empezar de nuevo limpia todo y vuelve al paso 1', async () => {
    const { fixture, el } = await crear();
    await elegir(fixture, 'Página web o tienda en línea');
    await elegir(fixture, 'Algo puntual, una sola función');
    await elegir(fixture, 'Sin afán, en los próximos meses');

    (el.querySelector('.cotizador-reiniciar') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(el.textContent).toContain(COTIZADOR.pasos[0].titulo);
    expect(el.textContent).toContain('1 de 3');
    expect(el.textContent).not.toContain('COP');
  });
});
