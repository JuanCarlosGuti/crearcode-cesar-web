import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { HERRAMIENTAS } from '../../../contenido/herramientas';
import { HerramientasPage } from './herramientas';

describe('HerramientasPage (centro de herramientas vivo, HU-43)', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  async function crear() {
    const fixture = TestBed.createComponent(HerramientasPage);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('muestra el titulo, la intro y una tarjeta por herramienta', async () => {
    const el = await crear();
    expect(el.textContent).toContain(HERRAMIENTAS.titulo);
    expect(el.textContent).toContain(HERRAMIENTAS.intro);
    expect(el.querySelectorAll('.herramienta-tarjeta')).toHaveLength(HERRAMIENTAS.tarjetas.length);
  });

  it('cada tarjeta con ancla enlaza a su herramienta en la misma pagina (auditoria QA5)', async () => {
    // Antes decian "Esta aqui abajo" sin enlace: el visitante tenia que
    // hacer scroll y buscar. El asistente vive en la burbuja flotante y
    // no tiene ancla; las demas si.
    const el = await crear();
    const conAncla = HERRAMIENTAS.tarjetas.filter((t) => t.ancla);
    expect(conAncla.length).toBeGreaterThanOrEqual(4);
    for (const tarjeta of conAncla) {
      expect(
        el.querySelector(`a[href="/herramientas#${tarjeta.ancla}"]`),
        `falta el enlace de "${tarjeta.titulo}"`,
      ).toBeTruthy();
    }
    expect(el.textContent).not.toContain('Está aquí abajo');
  });

  it('ya NO queda ninguna herramienta en Muy pronto: las 5 estan activas (F10 completa)', async () => {
    const el = await crear();
    expect(HERRAMIENTAS.tarjetas.every((t) => t.activa)).toBe(true);
    expect(el.querySelectorAll('.badge-muy-pronto')).toHaveLength(0);
  });

  it('integra el cotizador funcionando en la misma pagina (decision 11)', async () => {
    const el = await crear();
    expect(el.querySelector('app-cotizador')).toBeTruthy();
    expect(el.textContent).toContain('¿Qué tipo de proyecto tienes en mente?');
  });

  it('la banda de cuenta invita a registrarse con enlace a /registro', async () => {
    const el = await crear();
    expect(el.textContent).toContain(HERRAMIENTAS.cuenta.titulo);
    expect(el.querySelector('a[href="/registro"]')).toBeTruthy();
  });
});
