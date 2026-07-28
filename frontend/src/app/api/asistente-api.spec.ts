import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AsistenteApi } from './asistente-api';

describe('AsistenteApi', () => {
  let api: AsistenteApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(AsistenteApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('envia la conversacion con el header de sesion anonima', () => {
    api
      .enviar([{ rol: 'USUARIO', texto: '¿Qué servicios ofrecen?' }], 'sesion-123')
      .subscribe((respuesta) => {
        expect(respuesta.texto).toBe('Te cuento.');
        expect(respuesta.escalarAHumano).toBe(false);
      });

    const solicitud = httpMock.expectOne('/api/asistente/mensajes');
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.headers.get('X-Sesion-Anonima')).toBe('sesion-123');
    expect(solicitud.request.body).toEqual({
      mensajes: [{ rol: 'USUARIO', texto: '¿Qué servicios ofrecen?' }],
    });
    solicitud.flush({ texto: 'Te cuento.', escalarAHumano: false });
  });
});
