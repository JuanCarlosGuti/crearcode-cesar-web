import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';

import { clienteGuard } from './cliente.guard';
import { SesionService } from './sesion';

describe('clienteGuard', () => {
  let sesion: SesionService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    sesion = TestBed.inject(SesionService);
  });

  afterEach(() => {
    sessionStorage.clear();
    localStorage.clear();
  });

  it('permite el acceso con una sesion de cliente', () => {
    sesion.iniciarSesion({ token: 'token-cliente', rol: 'CLIENTE', correo: 'cliente@correo-de-prueba.com' });

    const resultado = TestBed.runInInjectionContext(() => clienteGuard({} as never, {} as never));

    expect(resultado).toBe(true);
  });

  it('redirige a /ingreso si no hay sesion', () => {
    const resultado = TestBed.runInInjectionContext(() => clienteGuard({} as never, {} as never));

    expect(resultado).not.toBe(true);
    const router = TestBed.inject(Router);
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/ingreso');
  });

  // El enlace de un correo a un proyecto, abierto sin sesion: tras
  // ingresar se vuelve al proyecto, no a /mi-cuenta (ISS-241).
  it('al mandar a /ingreso le dice a donde volver', () => {
    const resultado = TestBed.runInInjectionContext(() =>
      clienteGuard({} as never, { url: '/mi-cuenta/proyectos/7' } as never),
    );

    const destino = resultado as UrlTree;
    expect(destino.root.children['primary'].segments.map((s) => s.path)).toEqual(['ingreso']);
    expect(destino.queryParamMap.get('volver')).toBe('/mi-cuenta/proyectos/7');
  });

  it('redirige a /ingreso si la sesion es de admin', () => {
    sesion.iniciarSesion({ token: 'token-admin', rol: 'ADMIN', correo: 'admin@crearcode-cesar.local' });

    const resultado = TestBed.runInInjectionContext(() => clienteGuard({} as never, {} as never));

    expect(resultado).not.toBe(true);
    const router = TestBed.inject(Router);
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/ingreso');
  });
});
