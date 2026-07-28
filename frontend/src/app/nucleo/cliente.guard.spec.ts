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

  it('redirige a /ingreso si la sesion es de admin', () => {
    sesion.iniciarSesion({ token: 'token-admin', rol: 'ADMIN', correo: 'admin@crearcode-cesar.local' });

    const resultado = TestBed.runInInjectionContext(() => clienteGuard({} as never, {} as never));

    expect(resultado).not.toBe(true);
    const router = TestBed.inject(Router);
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/ingreso');
  });
});
