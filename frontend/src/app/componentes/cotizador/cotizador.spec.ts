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

  it('al completar los 3 pasos muestra el rango segun el alcance y el resumen', async () => {
    const { fixture, el } = await crear();
    await elegir(fixture, 'Automatización con IA');
    await elegir(fixture, 'Varias funciones conectadas');
    await elegir(fixture, 'En 4 a 8 semanas');

    expect(el.textContent).toContain('COP 9 – 25 millones');
    expect(el.textContent).toContain('a la medida');
    const resumen = el.querySelector('.cotizador-resumen')!.textContent!;
    expect(resumen).toContain('Automatización con IA');
    expect(resumen).toContain('Varias funciones conectadas');
    expect(resumen).toContain('En 4 a 8 semanas');
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
