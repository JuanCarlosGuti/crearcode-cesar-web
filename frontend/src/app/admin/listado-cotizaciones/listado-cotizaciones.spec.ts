import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Cotizacion } from '../../api/cotizaciones-api';
import { ListadoCotizacionesPage } from './listado-cotizaciones';

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
    validaHasta: '2026-08-26T10:00:00Z',
    enviadaEn: '2026-08-11T10:00:00Z',
    respondidaEn: null,
    items: [],
    subtotal: 5000000,
    impuesto: 950000,
    total: 5950000,
    ...parcial,
  };
}

describe('ListadoCotizacionesPage', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('lista las cotizaciones con su numero, cliente y total', async () => {
    const fixture = TestBed.createComponent(ListadoCotizacionesPage);
    await fixture.whenStable();

    httpMock.expectOne('/api/cotizaciones').flush([cotizacion()]);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.textContent).toContain('COT-2026-0001');
    expect(el.textContent).toContain('Panaderia El Trigal');
    // Formato colombiano, sin decimales.
    expect(el.textContent).toContain('5.950.000');
  });

  it('un borrador se muestra como tal, sin numero', async () => {
    const fixture = TestBed.createComponent(ListadoCotizacionesPage);
    await fixture.whenStable();

    httpMock.expectOne('/api/cotizaciones').flush([cotizacion({ numero: null, estado: 'BORRADOR' })]);
    await fixture.whenStable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Borrador');
  });

  it('filtrar por estado vuelve a pedir la lista con el parametro', async () => {
    const fixture = TestBed.createComponent(ListadoCotizacionesPage);
    await fixture.whenStable();
    httpMock.expectOne('/api/cotizaciones').flush([]);
    await fixture.whenStable();

    const select = (fixture.nativeElement as HTMLElement).querySelector('select') as HTMLSelectElement;
    select.value = 'ACEPTADA';
    select.dispatchEvent(new Event('change'));
    await fixture.whenStable();

    httpMock.expectOne('/api/cotizaciones?estado=ACEPTADA').flush([]);
  });

  it('sin resultados lo dice en vez de mostrar una tabla vacia', async () => {
    const fixture = TestBed.createComponent(ListadoCotizacionesPage);
    await fixture.whenStable();

    httpMock.expectOne('/api/cotizaciones').flush([]);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.textContent).toContain('Todavía no hay cotizaciones');
    expect(el.querySelector('table')).toBeNull();
  });

  it('si la carga falla muestra el error y no se queda cargando', async () => {
    const fixture = TestBed.createComponent(ListadoCotizacionesPage);
    await fixture.whenStable();

    httpMock.expectOne('/api/cotizaciones').flush('error', { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('[role="alert"]')?.textContent).toContain('No pudimos cargar');
  });
});
