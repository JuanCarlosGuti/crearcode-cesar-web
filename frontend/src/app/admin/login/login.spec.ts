import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';

import { SesionService } from '../../nucleo/sesion';
import { LoginPage } from './login';

function escribir(elemento: HTMLInputElement, valor: string): void {
  elemento.value = valor;
  elemento.dispatchEvent(new Event('input'));
}

describe('LoginPage', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('muestra los campos correo y contrasena con su label asociado', async () => {
    const fixture = TestBed.createComponent(LoginPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('#correo')).not.toBeNull();
    expect(el.querySelector('label[for="correo"]')).not.toBeNull();
    expect(el.querySelector('#contrasena')).not.toBeNull();
    expect(el.querySelector('label[for="contrasena"]')).not.toBeNull();
  });

  it('bloquea el envio si el correo o la contrasena estan vacios', async () => {
    const fixture = TestBed.createComponent(LoginPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    expect(el.querySelector('#correo')?.getAttribute('aria-invalid')).toBe('true');
    expect(el.querySelector('#contrasena')?.getAttribute('aria-invalid')).toBe('true');
    httpMock.expectNone('/api/auth/login');
  });

  it('con credenciales correctas guarda la sesion y navega a /admin', async () => {
    const fixture = TestBed.createComponent(LoginPage);
    await fixture.whenStable();
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');

    const el = fixture.nativeElement as HTMLElement;
    escribir(el.querySelector('#correo') as HTMLInputElement, 'admin@crearcode-cesar.local');
    escribir(el.querySelector('#contrasena') as HTMLInputElement, 'clave-correcta');
    await fixture.whenStable();
    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    httpMock.expectOne('/api/auth/login').flush({ token: 'token-de-prueba', expiraEn: '2026-07-17T18:00:00Z' });
    await fixture.whenStable();

    const sesion = TestBed.inject(SesionService);
    expect(sesion.estaAutenticado()).toBe(true);
    expect(sesion.obtenerToken()).toBe('token-de-prueba');
    expect(navigateSpy).toHaveBeenCalledWith('/admin');
  });

  it('con credenciales incorrectas muestra el mensaje generico y no navega', async () => {
    const fixture = TestBed.createComponent(LoginPage);
    await fixture.whenStable();
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');

    const el = fixture.nativeElement as HTMLElement;
    escribir(el.querySelector('#correo') as HTMLInputElement, 'admin@crearcode-cesar.local');
    escribir(el.querySelector('#contrasena') as HTMLInputElement, 'clave-incorrecta');
    await fixture.whenStable();
    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    httpMock.expectOne('/api/auth/login').flush('Correo o contraseña incorrectos', {
      status: 401,
      statusText: 'Unauthorized',
    });
    await fixture.whenStable();

    expect(el.textContent).toContain('Correo o contraseña incorrectos');
    expect(navigateSpy).not.toHaveBeenCalled();
    const sesion = TestBed.inject(SesionService);
    expect(sesion.estaAutenticado()).toBe(false);
  });
});
