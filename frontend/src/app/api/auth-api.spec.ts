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
    api.login('admin@crearcode-cesar.local', 'clave-secreta').subscribe((sesion) => {
      expect(sesion.rol).toBe('ADMIN');
      expect(sesion.correo).toBe('admin@crearcode-cesar.local');
    });

    const solicitud = httpMock.expectOne('/api/auth/login');
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.body).toEqual({ correo: 'admin@crearcode-cesar.local', contrasena: 'clave-secreta' });
    solicitud.flush({
      token: 'token-de-prueba',
      expiraEn: '2026-07-17T18:00:00Z',
      rol: 'ADMIN',
      correo: 'admin@crearcode-cesar.local',
    });
  });

  it('envia un POST a /api/auth/registro con el correo y la contrasena', () => {
    api.registrar('cliente@correo-de-prueba.com', 'contrasena-larga').subscribe();

    const solicitud = httpMock.expectOne('/api/auth/registro');
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.body).toEqual({
      correo: 'cliente@correo-de-prueba.com',
      contrasena: 'contrasena-larga',
    });
    solicitud.flush(null, { status: 201, statusText: 'Created' });
  });

  it('envia un POST a /api/auth/verificacion con el token', () => {
    api.verificarCorreo('token-del-enlace').subscribe();

    const solicitud = httpMock.expectOne('/api/auth/verificacion');
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.body).toEqual({ token: 'token-del-enlace' });
    solicitud.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('envia un POST a /api/auth/reenvio-verificacion con el correo', () => {
    api.reenviarVerificacion('cliente@correo-de-prueba.com').subscribe();

    const solicitud = httpMock.expectOne('/api/auth/reenvio-verificacion');
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.body).toEqual({ correo: 'cliente@correo-de-prueba.com' });
    solicitud.flush(null, { status: 202, statusText: 'Accepted' });
  });

  it('envia un POST a /api/auth/recuperacion con el correo', () => {
    api.solicitarRecuperacion('cliente@correo-de-prueba.com').subscribe();

    const solicitud = httpMock.expectOne('/api/auth/recuperacion');
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.body).toEqual({ correo: 'cliente@correo-de-prueba.com' });
    solicitud.flush(null, { status: 202, statusText: 'Accepted' });
  });

  it('envia un POST a /api/auth/restablecimiento con el token y la contrasena nueva', () => {
    api.restablecerContrasena('token-del-enlace', 'contrasena-nueva').subscribe();

    const solicitud = httpMock.expectOne('/api/auth/restablecimiento');
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.body).toEqual({ token: 'token-del-enlace', contrasena: 'contrasena-nueva' });
    solicitud.flush(null, { status: 204, statusText: 'No Content' });
  });
});
