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
  });

  it('permite el acceso si hay sesion iniciada', () => {
    sesion.iniciarSesion('token-de-prueba');

    const resultado = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));

    expect(resultado).toBe(true);
  });

  it('redirige a /admin/login si no hay sesion', () => {
    const resultado = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));

    expect(resultado).not.toBe(true);
    const router = TestBed.inject(Router);
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/admin/login');
  });
});
