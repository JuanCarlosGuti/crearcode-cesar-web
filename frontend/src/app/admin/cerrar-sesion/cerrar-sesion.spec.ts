import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { SesionService } from '../../nucleo/sesion';
import { CerrarSesionButton } from './cerrar-sesion';

describe('CerrarSesionButton', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('al hacer click limpia la sesion y navega a /admin/login', async () => {
    const sesion = TestBed.inject(SesionService);
    sesion.iniciarSesion({ token: 'token-de-prueba', rol: 'ADMIN', correo: 'admin@crearcode-cesar.local' });
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');

    const fixture = TestBed.createComponent(CerrarSesionButton);
    await fixture.whenStable();
    (fixture.nativeElement.querySelector('button') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(sesion.estaAutenticado()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith('/admin/login');
  });
});
