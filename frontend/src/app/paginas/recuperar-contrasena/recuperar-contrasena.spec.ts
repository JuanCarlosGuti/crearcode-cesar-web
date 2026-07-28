import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { RecuperarContrasenaPage } from './recuperar-contrasena';

function escribir(elemento: HTMLInputElement, valor: string): void {
  elemento.value = valor;
  elemento.dispatchEvent(new Event('input'));
}

describe('RecuperarContrasenaPage', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('muestra el campo de correo con su label', async () => {
    const fixture = TestBed.createComponent(RecuperarContrasenaPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('#correo')).not.toBeNull();
    expect(el.querySelector('label[for="correo"]')).not.toBeNull();
  });

  it('bloquea el envio con el correo vacio', async () => {
    const fixture = TestBed.createComponent(RecuperarContrasenaPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    httpMock.expectNone('/api/auth/recuperacion');
  });

  it('al enviar muestra siempre el mismo mensaje generico', async () => {
    const fixture = TestBed.createComponent(RecuperarContrasenaPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    escribir(el.querySelector('#correo') as HTMLInputElement, 'quien-sea@correo-de-prueba.com');
    await fixture.whenStable();
    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    const solicitud = httpMock.expectOne('/api/auth/recuperacion');
    expect(solicitud.request.body).toEqual({ correo: 'quien-sea@correo-de-prueba.com' });
    solicitud.flush(null, { status: 202, statusText: 'Accepted' });
    await fixture.whenStable();

    expect(el.textContent).toContain('Si tu correo está registrado, te llegará un enlace');
    expect(el.querySelector('form')).toBeNull();
  });
});
