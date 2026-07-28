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
    iniciarSesionAdmin();

    http.get('/api/solicitudes').subscribe();

    const solicitud = httpMock.expectOne('/api/solicitudes');
    expect(solicitud.request.headers.get('Authorization')).toBe('Bearer token-de-prueba');
    solicitud.flush([]);
  });

  it('no adjunta el token a la peticion de login aunque haya sesion', () => {
    iniciarSesionAdmin();

    http.post('/api/auth/login', { correo: 'a@b.com', contrasena: 'x' }).subscribe({ error: () => {} });

    const solicitud = httpMock.expectOne('/api/auth/login');
    expect(solicitud.request.headers.has('Authorization')).toBe(false);
    solicitud.flush('error', { status: 401, statusText: 'Unauthorized' });
  });

  it('no adjunta el token a ningun endpoint de /api/auth/ (registro, recuperacion, etc.)', () => {
    iniciarSesionAdmin();

    http.post('/api/auth/registro', { correo: 'a@b.com', contrasena: 'x' }).subscribe();

    const solicitud = httpMock.expectOne('/api/auth/registro');
    expect(solicitud.request.headers.has('Authorization')).toBe(false);
    solicitud.flush(null, { status: 201, statusText: 'Created' });
  });

  it('un 401 estando en el panel admin limpia la sesion y navega a /admin/login', () => {
    iniciarSesionAdmin();
    vi.spyOn(router, 'url', 'get').mockReturnValue('/admin/solicitudes/123');
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');

    http.get('/api/solicitudes').subscribe({ error: () => {} });

    httpMock.expectOne('/api/solicitudes').flush('No autenticado', { status: 401, statusText: 'Unauthorized' });

    expect(sesion.estaAutenticado()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith('/admin/login');
  });

  it('un 401 estando en /mi-cuenta limpia la sesion y navega a /ingreso', () => {
    iniciarSesionCliente();
    vi.spyOn(router, 'url', 'get').mockReturnValue('/mi-cuenta');
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');

    http.get('/api/solicitudes').subscribe({ error: () => {} });

    httpMock.expectOne('/api/solicitudes').flush('No autenticado', { status: 401, statusText: 'Unauthorized' });

    expect(sesion.estaAutenticado()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith('/ingreso');
  });

  it('un 401 estando en una pagina publica solo limpia la sesion, sin navegar', () => {
    iniciarSesionCliente();
    vi.spyOn(router, 'url', 'get').mockReturnValue('/contacto');
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');

    http.get('/api/solicitudes').subscribe({ error: () => {} });

    httpMock.expectOne('/api/solicitudes').flush('No autenticado', { status: 401, statusText: 'Unauthorized' });

    expect(sesion.estaAutenticado()).toBe(false);
    expect(navigateSpy).not.toHaveBeenCalled();
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

  function iniciarSesionAdmin(): void {
    sesion.iniciarSesion({ token: 'token-de-prueba', rol: 'ADMIN', correo: 'admin@crearcode-cesar.local' });
  }

  function iniciarSesionCliente(): void {
    sesion.iniciarSesion({ token: 'token-cliente', rol: 'CLIENTE', correo: 'cliente@correo-de-prueba.com' });
  }
});
