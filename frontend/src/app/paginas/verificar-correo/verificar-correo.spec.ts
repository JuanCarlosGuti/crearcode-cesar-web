import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { VerificarCorreoPage } from './verificar-correo';

function escribir(elemento: HTMLInputElement, valor: string): void {
  elemento.value = valor;
  elemento.dispatchEvent(new Event('input'));
}

describe('VerificarCorreoPage', () => {
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

  it('con token en la URL lo verifica y muestra el exito con enlace a ingreso', async () => {
    const fixture = TestBed.createComponent(VerificarCorreoPage);
    fixture.componentRef.setInput('token', 'token-del-enlace');
    await fixture.whenStable();

    const solicitud = httpMock.expectOne('/api/auth/verificacion');
    expect(solicitud.request.body).toEqual({ token: 'token-del-enlace' });
    solicitud.flush(null, { status: 204, statusText: 'No Content' });
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('¡Listo! Tu cuenta quedó verificada.');
    expect(el.querySelector('a[href="/ingreso"]')).not.toBeNull();
  });

  it('con token invalido muestra el error unico y el formulario de reenvio', async () => {
    const fixture = TestBed.createComponent(VerificarCorreoPage);
    fixture.componentRef.setInput('token', 'token-vencido');
    await fixture.whenStable();

    httpMock.expectOne('/api/auth/verificacion').flush('no', { status: 400, statusText: 'Bad Request' });
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Este enlace es inválido o ya venció.');
    expect(el.querySelector('#correo')).not.toBeNull();
  });

  it('sin token muestra directamente el error y el formulario de reenvio, sin llamar a la API', async () => {
    const fixture = TestBed.createComponent(VerificarCorreoPage);
    await fixture.whenStable();

    httpMock.expectNone('/api/auth/verificacion');
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Este enlace es inválido o ya venció.');
    expect(el.querySelector('#correo')).not.toBeNull();
  });

  it('el reenvio manda el correo y muestra siempre la respuesta generica', async () => {
    const fixture = TestBed.createComponent(VerificarCorreoPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    escribir(el.querySelector('#correo') as HTMLInputElement, 'cliente@correo-de-prueba.com');
    await fixture.whenStable();
    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    const solicitud = httpMock.expectOne('/api/auth/reenvio-verificacion');
    expect(solicitud.request.body).toEqual({ correo: 'cliente@correo-de-prueba.com' });
    solicitud.flush(null, { status: 202, statusText: 'Accepted' });
    await fixture.whenStable();

    expect(el.textContent).toContain('Si tu correo está registrado y pendiente de verificar');
  });
});
