import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { RestablecerContrasenaPage } from './restablecer-contrasena';

function escribir(elemento: HTMLInputElement, valor: string): void {
  elemento.value = valor;
  elemento.dispatchEvent(new Event('input'));
}

async function crearPaginaYEnviar(contrasena: string, confirmacion: string) {
  const fixture = TestBed.createComponent(RestablecerContrasenaPage);
  fixture.componentRef.setInput('token', 'token-del-enlace');
  await fixture.whenStable();
  const el = fixture.nativeElement as HTMLElement;
  escribir(el.querySelector('#contrasena') as HTMLInputElement, contrasena);
  escribir(el.querySelector('#confirmacion') as HTMLInputElement, confirmacion);
  await fixture.whenStable();
  (el.querySelector('form') as HTMLFormElement).requestSubmit();
  await fixture.whenStable();
  return { fixture, el };
}

describe('RestablecerContrasenaPage', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('muestra los dos campos de contrasena con su label', async () => {
    const fixture = TestBed.createComponent(RestablecerContrasenaPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('#contrasena')).not.toBeNull();
    expect(el.querySelector('#confirmacion')).not.toBeNull();
  });

  it('marca error si las contrasenas no coinciden y no llama a la API', async () => {
    const { el } = await crearPaginaYEnviar('contrasena-larga', 'otra-distinta-larga');

    expect(el.textContent).toContain('Las contraseñas no coinciden.');
    httpMock.expectNone('/api/auth/restablecimiento');
  });

  it('con datos validos restablece y muestra el exito con enlace a ingreso', async () => {
    const { fixture, el } = await crearPaginaYEnviar('contrasena-nueva-larga', 'contrasena-nueva-larga');

    const solicitud = httpMock.expectOne('/api/auth/restablecimiento');
    expect(solicitud.request.body).toEqual({
      token: 'token-del-enlace',
      contrasena: 'contrasena-nueva-larga',
    });
    solicitud.flush(null, { status: 204, statusText: 'No Content' });
    await fixture.whenStable();

    expect(el.textContent).toContain('Tu contraseña quedó actualizada.');
    expect(el.querySelector('a[href="/ingreso"]')).not.toBeNull();
    expect(el.querySelector('form')).toBeNull();
  });

  it('un 400 muestra el error unico de enlace invalido', async () => {
    const { fixture, el } = await crearPaginaYEnviar('contrasena-nueva-larga', 'contrasena-nueva-larga');

    httpMock.expectOne('/api/auth/restablecimiento').flush('no', { status: 400, statusText: 'Bad Request' });
    await fixture.whenStable();

    expect(el.textContent).toContain('Este enlace es inválido o ya venció.');
  });
});
