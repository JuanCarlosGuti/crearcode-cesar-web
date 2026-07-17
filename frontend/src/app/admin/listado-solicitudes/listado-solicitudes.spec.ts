import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { Solicitud } from '../../api/solicitudes-api';
import { ListadoSolicitudesPage } from './listado-solicitudes';

const SOLICITUD_EJEMPLO: Solicitud = {
  id: '11111111-1111-1111-1111-111111111111',
  nombre: 'Ana Pérez',
  empresa: null,
  correo: 'ana@empresa.com',
  telefono: '3001234567',
  servicioDeInteres: 'OTRO',
  mensaje: 'Necesito ayuda.',
  estado: 'NUEVA',
  fechaCreacion: '2026-07-17T10:00:00Z',
  fechaUltimaActualizacion: '2026-07-17T10:00:00Z',
};

describe('ListadoSolicitudesPage', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('pide el listado sin filtro al iniciar y muestra las solicitudes recibidas', async () => {
    const fixture = TestBed.createComponent(ListadoSolicitudesPage);
    fixture.detectChanges();

    httpMock.expectOne('/api/solicitudes').flush([SOLICITUD_EJEMPLO]);
    await fixture.whenStable();

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Ana Pérez');
    expect(texto).toContain('NUEVA');
  });

  it('muestra un estado vacio general cuando no hay solicitudes', async () => {
    const fixture = TestBed.createComponent(ListadoSolicitudesPage);
    fixture.detectChanges();

    httpMock.expectOne('/api/solicitudes').flush([]);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Todavía no ha llegado ninguna solicitud');
  });

  it('cada fila enlaza al detalle de la solicitud', async () => {
    const fixture = TestBed.createComponent(ListadoSolicitudesPage);
    fixture.detectChanges();

    httpMock.expectOne('/api/solicitudes').flush([SOLICITUD_EJEMPLO]);
    await fixture.whenStable();

    const enlace = fixture.nativeElement.querySelector('a[href="/admin/solicitudes/11111111-1111-1111-1111-111111111111"]');
    expect(enlace).not.toBeNull();
  });

  it('al filtrar por estado pide el listado filtrado', async () => {
    const fixture = TestBed.createComponent(ListadoSolicitudesPage);
    fixture.detectChanges();
    httpMock.expectOne('/api/solicitudes').flush([SOLICITUD_EJEMPLO]);
    await fixture.whenStable();

    const select = fixture.nativeElement.querySelector('#filtro-estado') as HTMLSelectElement;
    select.value = 'DESCARTADA';
    select.dispatchEvent(new Event('change'));
    await fixture.whenStable();

    const solicitud = httpMock.expectOne(
      (req) => req.url === '/api/solicitudes' && req.params.get('estado') === 'DESCARTADA',
    );
    solicitud.flush([]);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('No hay solicitudes en este estado por ahora');
  });
});
