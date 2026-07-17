import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';

import { SesionService } from './sesion';
import { tokenInterceptor } from './token.interceptor';

describe('tokenInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let sesion: SesionService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([tokenInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    sesion = TestBed.inject(SesionService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('no adjunta Authorization si no hay sesion', () => {
    http.get('/api/solicitudes').subscribe();

    const solicitud = httpMock.expectOne('/api/solicitudes');
    expect(solicitud.request.headers.has('Authorization')).toBe(false);
    solicitud.flush([]);
  });

  it('adjunta el token como Bearer cuando hay sesion', () => {
    sesion.iniciarSesion('token-de-prueba');

    http.get('/api/solicitudes').subscribe();

    const solicitud = httpMock.expectOne('/api/solicitudes');
    expect(solicitud.request.headers.get('Authorization')).toBe('Bearer token-de-prueba');
    solicitud.flush([]);
  });

  it('no adjunta el token a la peticion de login aunque haya sesion', () => {
    sesion.iniciarSesion('token-de-prueba');

    http.post('/api/auth/login', { correo: 'a@b.com', contrasena: 'x' }).subscribe({ error: () => {} });

    const solicitud = httpMock.expectOne('/api/auth/login');
    expect(solicitud.request.headers.has('Authorization')).toBe(false);
    solicitud.flush('error', { status: 401, statusText: 'Unauthorized' });
  });

  it('un 401 fuera del login limpia la sesion y navega a /admin/login', async () => {
    sesion.iniciarSesion('token-expirado');
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');

    http.get('/api/solicitudes').subscribe({ error: () => {} });

    httpMock.expectOne('/api/solicitudes').flush('No autenticado', { status: 401, statusText: 'Unauthorized' });

    expect(sesion.estaAutenticado()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith('/admin/login');
  });

  it('un 401 en el login NO limpia la sesion ni navega (lo maneja la propia pagina de login)', () => {
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');

    http.post('/api/auth/login', { correo: 'a@b.com', contrasena: 'mala' }).subscribe({ error: () => {} });

    httpMock.expectOne('/api/auth/login').flush('Credenciales invalidas', {
      status: 401,
      statusText: 'Unauthorized',
    });

    expect(navigateSpy).not.toHaveBeenCalled();
  });
});
