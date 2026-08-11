import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Cotizacion } from '../../api/cotizaciones-api';
import { MisCotizacionesPage } from './mis-cotizaciones';

const EN_UN_MES = new Date(Date.now() + 30 * 24 * 3600 * 1000).toISOString();
const HACE_UN_MES = new Date(Date.now() - 30 * 24 * 3600 * 1000).toISOString();

function cotizacion(parcial: Partial<Cotizacion> = {}): Cotizacion {
  return {
    id: 'id-1',
    numero: 'COT-2026-0001',
    estado: 'ENVIADA',
    origenSolicitudId: null,
    clienteNombre: 'Panaderia El Trigal',
    clienteCorreo: 'cliente@empresa.com',
    clienteTelefono: null,
    clienteIdentificacion: null,
    impuestoPorcentaje: 19,
    notas: null,
    creadaEn: '2026-08-11T10:00:00Z',
    validaHasta: EN_UN_MES,
    enviadaEn: '2026-08-11T10:00:00Z',
    respondidaEn: null,
    items: [{ descripcion: 'Desarrollo', cantidad: 1, valorUnitario: 5000000, subtotal: 5000000 }],
    subtotal: 5000000,
    impuesto: 950000,
    total: 5950000,
    ...parcial,
  };
}

async function crearCon(cotizaciones: Cotizacion[]) {
  const fixture = TestBed.createComponent(MisCotizacionesPage);
  await fixture.whenStable();

  const httpMock = TestBed.inject(HttpTestingController);
  httpMock.expectOne('/api/mis-cotizaciones').flush(cotizaciones);
  await fixture.whenStable();

  return { fixture, el: fixture.nativeElement as HTMLElement, httpMock };
}

async function abrirDetalle(fixture: Awaited<ReturnType<typeof crearCon>>['fixture']) {
  const el = fixture.nativeElement as HTMLElement;
  const ver = Array.from(el.querySelectorAll('button')).find((b) =>
    b.textContent?.includes('Ver detalle'),
  ) as HTMLButtonElement;
  ver.click();
  await fixture.whenStable();
}

describe('MisCotizacionesPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    globalThis.confirm = () => true;
  });

  it('lista las cotizaciones del cliente con su total', async () => {
    const { el } = await crearCon([cotizacion()]);

    expect(el.textContent).toContain('COT-2026-0001');
    expect(el.textContent).toContain('5.950.000');
  });

  it('sin cotizaciones lo explica en vez de mostrar una lista vacia', async () => {
    const { el } = await crearCon([]);

    expect(el.textContent).toContain('Todavía no tienes cotizaciones');
  });

  it('el detalle muestra los items y permite aceptar', async () => {
    const { fixture, el, httpMock } = await crearCon([cotizacion()]);

    await abrirDetalle(fixture);
    expect(el.textContent).toContain('Desarrollo');

    const aceptar = Array.from(el.querySelectorAll('button')).find((b) =>
      b.textContent?.includes('Aceptar'),
    ) as HTMLButtonElement;
    aceptar.click();
    await fixture.whenStable();

    const peticion = httpMock.expectOne('/api/mis-cotizaciones/id-1/aceptacion');
    expect(peticion.request.method).toBe('POST');
    peticion.flush(cotizacion({ estado: 'ACEPTADA', respondidaEn: new Date().toISOString() }));
    await fixture.whenStable();

    httpMock.expectOne('/api/mis-cotizaciones').flush([]);
  });

  it('rechazar llama a su propio endpoint', async () => {
    const { fixture, el, httpMock } = await crearCon([cotizacion()]);
    await abrirDetalle(fixture);

    const rechazar = Array.from(el.querySelectorAll('button')).find((b) =>
      b.textContent?.trim() === 'Rechazar',
    ) as HTMLButtonElement;
    rechazar.click();
    await fixture.whenStable();

    expect(httpMock.expectOne('/api/mis-cotizaciones/id-1/rechazo').request.method).toBe('POST');
  });

  // La validez tambien se comprueba en el servidor; aqui es solo para
  // no ofrecer un boton que va a fallar.
  it('una cotizacion vencida no ofrece aceptar ni rechazar', async () => {
    const { fixture, el } = await crearCon([cotizacion({ validaHasta: HACE_UN_MES })]);
    await abrirDetalle(fixture);

    expect(Array.from(el.querySelectorAll('button')).some((b) => b.textContent?.includes('Aceptar')))
      .toBe(false);
    expect(el.textContent).toContain('Esta cotización venció');
  });

  it('una cotizacion ya respondida tampoco se puede volver a responder', async () => {
    const { fixture, el } = await crearCon([
      cotizacion({ estado: 'ACEPTADA', respondidaEn: '2026-08-12T10:00:00Z' }),
    ]);
    await abrirDetalle(fixture);

    expect(Array.from(el.querySelectorAll('button')).some((b) => b.textContent?.includes('Aceptar')))
      .toBe(false);
    expect(el.textContent).toContain('Ya respondiste');
  });

  it('si la carga falla lo dice', async () => {
    const fixture = TestBed.createComponent(MisCotizacionesPage);
    await fixture.whenStable();
    TestBed.inject(HttpTestingController)
      .expectOne('/api/mis-cotizaciones')
      .flush('error', { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();

    expect((fixture.nativeElement as HTMLElement).querySelector('[role="alert"]')?.textContent)
      .toContain('No pudimos cargar');
  });
});
