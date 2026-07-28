import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { RegistroPage } from './registro';

function escribir(elemento: HTMLInputElement, valor: string): void {
  elemento.value = valor;
  elemento.dispatchEvent(new Event('input'));
}

async function crearPagina() {
  const fixture = TestBed.createComponent(RegistroPage);
  await fixture.whenStable();
  return { fixture, el: fixture.nativeElement as HTMLElement };
}

async function llenarFormularioValido(fixture: Awaited<ReturnType<typeof crearPagina>>['fixture']) {
  const el = fixture.nativeElement as HTMLElement;
  escribir(el.querySelector('#correo') as HTMLInputElement, 'cliente@correo-de-prueba.com');
  escribir(el.querySelector('#contrasena') as HTMLInputElement, 'contrasena-larga');
  escribir(el.querySelector('#confirmacion') as HTMLInputElement, 'contrasena-larga');
  const politica = el.querySelector('#aceptaPolitica') as HTMLInputElement;
  politica.click();
  await fixture.whenStable();
}

describe('RegistroPage', () => {
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

  it('muestra los campos con su label asociado y el enlace a la politica', async () => {
    const { el } = await crearPagina();

    expect(el.querySelector('#correo')).not.toBeNull();
    expect(el.querySelector('label[for="correo"]')).not.toBeNull();
    expect(el.querySelector('#contrasena')).not.toBeNull();
    expect(el.querySelector('#confirmacion')).not.toBeNull();
    expect(el.querySelector('#aceptaPolitica')).not.toBeNull();
    expect(el.querySelector('a[href="/legales/politica-de-datos"]')).not.toBeNull();
  });

  it('bloquea el envio si los campos estan vacios', async () => {
    const { fixture, el } = await crearPagina();

    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    expect(el.querySelector('#correo')?.getAttribute('aria-invalid')).toBe('true');
    httpMock.expectNone('/api/auth/registro');
  });

  it('marca error si las contrasenas no coinciden', async () => {
    const { fixture, el } = await crearPagina();
    escribir(el.querySelector('#correo') as HTMLInputElement, 'cliente@correo-de-prueba.com');
    escribir(el.querySelector('#contrasena') as HTMLInputElement, 'contrasena-larga');
    escribir(el.querySelector('#confirmacion') as HTMLInputElement, 'otra-distinta-larga');
    (el.querySelector('#aceptaPolitica') as HTMLInputElement).click();
    await fixture.whenStable();

    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    expect(el.textContent).toContain('Las contraseñas no coinciden.');
    httpMock.expectNone('/api/auth/registro');
  });

  it('marca error si la contrasena es mas corta que 10 caracteres', async () => {
    const { fixture, el } = await crearPagina();
    escribir(el.querySelector('#correo') as HTMLInputElement, 'cliente@correo-de-prueba.com');
    escribir(el.querySelector('#contrasena') as HTMLInputElement, 'corta');
    escribir(el.querySelector('#confirmacion') as HTMLInputElement, 'corta');
    (el.querySelector('#aceptaPolitica') as HTMLInputElement).click();
    await fixture.whenStable();

    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    expect(el.textContent).toContain('La contraseña debe tener al menos 10 caracteres.');
    httpMock.expectNone('/api/auth/registro');
  });

  it('con datos validos envia el registro y muestra la confirmacion con el correo', async () => {
    const { fixture, el } = await crearPagina();
    await llenarFormularioValido(fixture);

    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    const solicitud = httpMock.expectOne('/api/auth/registro');
    expect(solicitud.request.body).toEqual({
      correo: 'cliente@correo-de-prueba.com',
      contrasena: 'contrasena-larga',
    });
    solicitud.flush(null, { status: 201, statusText: 'Created' });
    await fixture.whenStable();

    expect(el.textContent).toContain('¡Ya casi!');
    expect(el.textContent).toContain('cliente@correo-de-prueba.com');
    expect(el.querySelector('form')).toBeNull();
  });

  it('un 409 muestra el aviso de cuenta existente con enlaces a ingreso y recuperacion', async () => {
    const { fixture, el } = await crearPagina();
    await llenarFormularioValido(fixture);

    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    httpMock.expectOne('/api/auth/registro').flush('conflicto', { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();

    expect(el.textContent).toContain('Ya existe una cuenta con este correo.');
    expect(el.querySelector('a[href="/ingreso"]')).not.toBeNull();
    expect(el.querySelector('a[href="/recuperar-contrasena"]')).not.toBeNull();
  });
});
