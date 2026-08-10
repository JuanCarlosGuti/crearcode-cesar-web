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

  it('marca Muy pronto SOLO las herramientas que no estan activas (honestidad)', async () => {
    const el = await crear();
    const esperadas = HERRAMIENTAS.tarjetas.filter((t) => !t.activa).length;
    expect(el.querySelectorAll('.badge-muy-pronto')).toHaveLength(esperadas);
    expect(esperadas).toBeGreaterThan(0);
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
