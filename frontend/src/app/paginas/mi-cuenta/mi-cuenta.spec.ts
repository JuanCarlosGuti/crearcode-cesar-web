import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { SesionService } from '../../nucleo/sesion';
import { MiCuentaPage } from './mi-cuenta';

describe('MiCuentaPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('muestra el correo de la sesion iniciada y el enlace de recuperacion', async () => {
    TestBed.inject(SesionService).iniciarSesion({
      token: 'token-cliente',
      rol: 'CLIENTE',
      correo: 'cliente@correo-de-prueba.com',
    });

    const fixture = TestBed.createComponent(MiCuentaPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.textContent).toContain('Sesión iniciada como');
    expect(el.textContent).toContain('cliente@correo-de-prueba.com');
    expect(el.querySelector('a[href="/recuperar-contrasena"]')).not.toBeNull();
  });

  it('cerrar sesion limpia la sesion y navega al inicio', async () => {
    const sesion = TestBed.inject(SesionService);
    sesion.iniciarSesion({ token: 'token-cliente', rol: 'CLIENTE', correo: 'cliente@correo-de-prueba.com' });
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');

    const fixture = TestBed.createComponent(MiCuentaPage);
    await fixture.whenStable();
    (fixture.nativeElement.querySelector('button') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(sesion.estaAutenticado()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith('/');
  });
});
