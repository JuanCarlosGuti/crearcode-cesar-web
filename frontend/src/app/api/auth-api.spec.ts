import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AuthApi } from './auth-api';

describe('AuthApi', () => {
  let api: AuthApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(AuthApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('envia un POST a /api/auth/login con el correo y la contrasena', () => {
    api.login('admin@crearcode-cesar.local', 'clave-secreta').subscribe();

    const solicitud = httpMock.expectOne('/api/auth/login');
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.body).toEqual({ correo: 'admin@crearcode-cesar.local', contrasena: 'clave-secreta' });
    solicitud.flush({ token: 'token-de-prueba', expiraEn: '2026-07-17T18:00:00Z' });
  });
});
