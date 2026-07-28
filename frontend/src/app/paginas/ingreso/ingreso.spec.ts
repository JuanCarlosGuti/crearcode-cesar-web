import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';

import { SesionService } from '../../nucleo/sesion';
import { IngresoPage } from './ingreso';

function escribir(elemento: HTMLInputElement, valor: string): void {
  elemento.value = valor;
  elemento.dispatchEvent(new Event('input'));
}

async function crearPaginaConCredenciales(correo: string, contrasena: string) {
  const fixture = TestBed.createComponent(IngresoPage);
  await fixture.whenStable();
  const el = fixture.nativeElement as HTMLElement;
  escribir(el.querySelector('#correo') as HTMLInputElement, correo);
  escribir(el.querySelector('#contrasena') as HTMLInputElement, contrasena);
  await fixture.whenStable();
  (el.querySelector('form') as HTMLFormElement).requestSubmit();
  await fixture.whenStable();
  return { fixture, el };
}

describe('IngresoPage', () => {
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

  it('muestra los campos y los enlaces a registro y recuperacion', async () => {
    const fixture = TestBed.createComponent(IngresoPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('#correo')).not.toBeNull();
    expect(el.querySelector('#contrasena')).not.toBeNull();
    expect(el.querySelector('a[href="/registro"]')).not.toBeNull();
    expect(el.querySelector('a[href="/recuperar-contrasena"]')).not.toBeNull();
  });

  it('un cliente entra y navega a /mi-cuenta', async () => {
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');
    const { fixture } = await crearPaginaConCredenciales('cliente@correo-de-prueba.com', 'contrasena-larga');

    httpMock.expectOne('/api/auth/login').flush({
      token: 'token-cliente',
      expiraEn: '2026-07-29T18:00:00Z',
      rol: 'CLIENTE',
      correo: 'cliente@correo-de-prueba.com',
    });
    await fixture.whenStable();

    const sesion = TestBed.inject(SesionService);
    expect(sesion.esCliente()).toBe(true);
    expect(navigateSpy).toHaveBeenCalledWith('/mi-cuenta');
  });

  it('un admin entra y navega a /admin', async () => {
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');
    const { fixture } = await crearPaginaConCredenciales('admin@crearcode-cesar.local', 'clave-correcta');

    httpMock.expectOne('/api/auth/login').flush({
      token: 'token-admin',
      expiraEn: '2026-07-29T18:00:00Z',
      rol: 'ADMIN',
      correo: 'admin@crearcode-cesar.local',
    });
    await fixture.whenStable();

    expect(navigateSpy).toHaveBeenCalledWith('/admin');
  });

  it('un 401 muestra el mensaje generico sin navegar', async () => {
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');
    const { fixture, el } = await crearPaginaConCredenciales('cliente@correo-de-prueba.com', 'clave-mala');

    httpMock.expectOne('/api/auth/login').flush('no', { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();

    expect(el.textContent).toContain('Correo o contraseña incorrectos.');
    expect(navigateSpy).not.toHaveBeenCalled();
    expect(TestBed.inject(SesionService).estaAutenticado()).toBe(false);
  });

  it('un 403 muestra el aviso de cuenta sin verificar con el enlace de reenvio', async () => {
    const { fixture, el } = await crearPaginaConCredenciales('cliente@correo-de-prueba.com', 'contrasena-larga');

    httpMock.expectOne('/api/auth/login').flush('sin verificar', { status: 403, statusText: 'Forbidden' });
    await fixture.whenStable();

    expect(el.textContent).toContain('Tu cuenta aún no está verificada.');
    expect(el.querySelector('a[href="/verificar-correo"]')).not.toBeNull();
  });
});
