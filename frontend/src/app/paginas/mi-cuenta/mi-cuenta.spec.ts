import { vi } from 'vitest';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { SesionService } from '../../nucleo/sesion';
import { MiCuentaPage } from './mi-cuenta';

describe('MiCuentaPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
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

  function conSesionDeCliente() {
    TestBed.inject(SesionService).iniciarSesion({
      token: 'token-cliente',
      rol: 'CLIENTE',
      correo: 'cliente@correo-de-prueba.com',
    });
  }

  /**
   * Borrar la cuenta no se deshace: no puede pasar por pulsar un boton
   * una vez. La confirmacion va en la pagina y no en un window.confirm
   * — ese no se puede estilar, no siempre se lee y el navegador puede
   * suprimirlo.
   */
  it('no borra nada al primer clic: primero pide confirmacion', async () => {
    conSesionDeCliente();
    const httpMock = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(MiCuentaPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    const boton = Array.from(el.querySelectorAll('button')).find((b) =>
      b.textContent?.includes('Eliminar mi cuenta'),
    );
    boton!.click();
    await fixture.whenStable();

    httpMock.expectNone('/api/mi-cuenta');
    expect(el.textContent).toContain('no se puede deshacer');
  });

  it('al confirmar, borra la cuenta, cierra la sesion y saca al visitante del area privada', async () => {
    conSesionDeCliente();
    const httpMock = TestBed.inject(HttpTestingController);
    const router = TestBed.inject(Router);
    const navegaciones: string[] = [];
    vi.spyOn(router, 'navigateByUrl').mockImplementation((url) => {
      navegaciones.push(String(url));
      return Promise.resolve(true);
    });
    const fixture = TestBed.createComponent(MiCuentaPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    Array.from(el.querySelectorAll('button'))
      .find((b) => b.textContent?.includes('Eliminar mi cuenta'))!
      .click();
    await fixture.whenStable();
    Array.from(el.querySelectorAll('button'))
      .find((b) => b.textContent?.includes('Sí, eliminar'))!
      .click();
    await fixture.whenStable();

    const peticion = httpMock.expectOne('/api/mi-cuenta');
    expect(peticion.request.method).toBe('DELETE');
    // Sin cuerpo: el correo sale del token, no de la peticion.
    expect(peticion.request.body).toBeNull();
    peticion.flush(null);
    await fixture.whenStable();

    expect(TestBed.inject(SesionService).estaAutenticado()).toBe(false);
    expect(navegaciones).toContain('/');
  });

  it('si el borrado falla lo dice y mantiene la sesion', async () => {
    conSesionDeCliente();
    const httpMock = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(MiCuentaPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    Array.from(el.querySelectorAll('button'))
      .find((b) => b.textContent?.includes('Eliminar mi cuenta'))!
      .click();
    await fixture.whenStable();
    Array.from(el.querySelectorAll('button'))
      .find((b) => b.textContent?.includes('Sí, eliminar'))!
      .click();
    await fixture.whenStable();

    httpMock.expectOne('/api/mi-cuenta').flush('boom', { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();

    expect(el.textContent).toContain('No pudimos eliminar la cuenta');
    expect(TestBed.inject(SesionService).estaAutenticado()).toBe(true);
  });
});
