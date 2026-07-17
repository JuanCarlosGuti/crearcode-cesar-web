import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { SolicitudesApi } from './solicitudes-api';

describe('SolicitudesApi', () => {
  let api: SolicitudesApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(SolicitudesApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('envia un POST a /api/solicitudes con el payload dado', () => {
    const payload = {
      nombre: 'Ana Pérez',
      empresa: '',
      correo: 'ana@empresa.com',
      telefono: '3001234567',
      servicioDeInteres: 'OTRO',
      mensaje: 'Necesito ayuda.',
      aceptaConsentimiento: true,
      sitioWeb: '',
    };

    api.registrar(payload).subscribe();

    const solicitud = httpMock.expectOne('/api/solicitudes');
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.body).toEqual(payload);
    solicitud.flush({ id: '11111111-1111-1111-1111-111111111111' });
  });

  it('listar envia un GET a /api/solicitudes sin filtro por defecto', () => {
    api.listar().subscribe();

    const solicitud = httpMock.expectOne('/api/solicitudes');
    expect(solicitud.request.method).toBe('GET');
    solicitud.flush([]);
  });

  it('listar con estado agrega el query param estado', () => {
    api.listar('NUEVA').subscribe();

    const solicitud = httpMock.expectOne((req) => req.url === '/api/solicitudes' && req.params.get('estado') === 'NUEVA');
    expect(solicitud.request.method).toBe('GET');
    solicitud.flush([]);
  });

  it('cambiarEstado envia un PATCH con el nuevo estado', () => {
    api.cambiarEstado('11111111-1111-1111-1111-111111111111', 'CONTACTADA').subscribe();

    const solicitud = httpMock.expectOne('/api/solicitudes/11111111-1111-1111-1111-111111111111/estado');
    expect(solicitud.request.method).toBe('PATCH');
    expect(solicitud.request.body).toEqual({ nuevoEstado: 'CONTACTADA' });
    solicitud.flush(null);
  });
});
