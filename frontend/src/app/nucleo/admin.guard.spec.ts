import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';

import { adminGuard } from './admin.guard';
import { SesionService } from './sesion';

describe('adminGuard', () => {
  let sesion: SesionService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    sesion = TestBed.inject(SesionService);
  });

  afterEach(() => {
    sessionStorage.clear();
    localStorage.clear();
  });

  it('permite el acceso con una sesion de admin', () => {
    sesion.iniciarSesion({ token: 'token-de-prueba', rol: 'ADMIN', correo: 'admin@crearcode-cesar.local' });

    const resultado = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));

    expect(resultado).toBe(true);
  });

  it('redirige a /admin/login si no hay sesion', () => {
    const resultado = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));

    expect(resultado).not.toBe(true);
    const router = TestBed.inject(Router);
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/admin/login');
  });

  // El correo de la respuesta de un cliente lleva al proyecto en el
  // panel: tras ingresar se vuelve ahi (ISS-241).
  it('al mandar a /admin/login le dice a donde volver', () => {
    const resultado = TestBed.runInInjectionContext(() =>
      adminGuard({} as never, { url: '/admin/proyectos/3' } as never),
    );

    const destino = resultado as UrlTree;
    expect(destino.root.children['primary'].segments.map((s) => s.path)).toEqual(['admin', 'login']);
    expect(destino.queryParamMap.get('volver')).toBe('/admin/proyectos/3');
  });

  it('redirige a /admin/login si la sesion es de cliente (no basta estar autenticado)', () => {
    sesion.iniciarSesion({ token: 'token-cliente', rol: 'CLIENTE', correo: 'cliente@correo-de-prueba.com' });

    const resultado = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));

    expect(resultado).not.toBe(true);
    const router = TestBed.inject(Router);
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/admin/login');
  });
});
