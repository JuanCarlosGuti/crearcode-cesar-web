import { Component, PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { AparecerAlVer } from './aparecer-al-ver';

@Component({
  template: '<section aparecerAlVer>Contenido</section>',
  imports: [AparecerAlVer],
})
class PaginaDePrueba {}

type CallbackObservador = (entradas: { isIntersecting: boolean }[]) => void;

describe('AparecerAlVer', () => {
  let callbackCapturado: CallbackObservador | null;
  let elementosObservados: Element[];
  let desconectado: boolean;

  beforeEach(() => {
    callbackCapturado = null;
    elementosObservados = [];
    desconectado = false;
    vi.stubGlobal(
      'IntersectionObserver',
      class {
        constructor(callback: CallbackObservador) {
          callbackCapturado = callback;
        }
        observe(elemento: Element): void {
          elementosObservados.push(elemento);
        }
        disconnect(): void {
          desconectado = true;
        }
      },
    );
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
    delete (window as { matchMedia?: unknown }).matchMedia;
  });

  // jsdom no trae matchMedia: se define aquí con el valor que cada
  // test necesite (por defecto, sin preferencia de reducir movimiento).
  function definirMatchMedia(prefiereMenosMovimiento: boolean): void {
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      configurable: true,
      value: vi.fn().mockReturnValue({ matches: prefiereMenosMovimiento } as MediaQueryList),
    });
  }

  function seccion(fixture: { nativeElement: HTMLElement }): HTMLElement {
    return fixture.nativeElement.querySelector('section') as HTMLElement;
  }

  it('oculta el elemento al iniciar y lo revela cuando entra al viewport', async () => {
    definirMatchMedia(false);
    const fixture = TestBed.createComponent(PaginaDePrueba);
    await fixture.whenStable();

    const el = seccion(fixture);
    expect(el.classList.contains('aparecer-oculto')).toBe(true);
    expect(elementosObservados).toContain(el);

    callbackCapturado!([{ isIntersecting: true }]);

    expect(el.classList.contains('aparecer-visible')).toBe(true);
    expect(el.classList.contains('aparecer-oculto')).toBe(false);
    expect(desconectado).toBe(true);
  });

  it('no oculta nada mientras el elemento no entre al viewport', async () => {
    definirMatchMedia(false);
    const fixture = TestBed.createComponent(PaginaDePrueba);
    await fixture.whenStable();

    callbackCapturado!([{ isIntersecting: false }]);

    expect(seccion(fixture).classList.contains('aparecer-visible')).toBe(false);
    expect(seccion(fixture).classList.contains('aparecer-oculto')).toBe(true);
  });

  it('con prefers-reduced-motion no toca las clases: el contenido queda visible', async () => {
    definirMatchMedia(true);

    const fixture = TestBed.createComponent(PaginaDePrueba);
    await fixture.whenStable();

    expect(seccion(fixture).classList.contains('aparecer-oculto')).toBe(false);
    expect(elementosObservados).toEqual([]);
  });

  it('en el servidor (SSR) es un no-op: el HTML prerenderizado nunca se oculta', async () => {
    TestBed.overrideProvider(PLATFORM_ID, { useValue: 'server' });

    const fixture = TestBed.createComponent(PaginaDePrueba);
    await fixture.whenStable();

    expect(seccion(fixture).classList.contains('aparecer-oculto')).toBe(false);
    expect(elementosObservados).toEqual([]);
  });
});
