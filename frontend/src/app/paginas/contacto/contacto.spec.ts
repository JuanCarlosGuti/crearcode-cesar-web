import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ContactoPage } from './contacto';

function escribir(elemento: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement, valor: string): void {
  elemento.value = valor;
  elemento.dispatchEvent(new Event('input'));
}

function difuminar(elemento: HTMLElement): void {
  elemento.dispatchEvent(new Event('blur'));
}

function llenarFormularioValido(el: HTMLElement): void {
  escribir(el.querySelector('#nombre') as HTMLInputElement, 'Ana Pérez');
  escribir(el.querySelector('#correo') as HTMLInputElement, 'ana@empresa.com');
  escribir(el.querySelector('#telefono') as HTMLInputElement, '3001234567');
  escribir(el.querySelector('#servicioDeInteres') as HTMLSelectElement, 'OTRO');
  escribir(el.querySelector('#mensaje') as HTMLTextAreaElement, 'Necesito ayuda con mi negocio.');
  const consentimiento = el.querySelector('#aceptaConsentimiento') as HTMLInputElement;
  consentimiento.checked = true;
  consentimiento.dispatchEvent(new Event('input'));
}

describe('ContactoPage', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('muestra todos los campos obligatorios con su label asociado', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    ['nombre', 'correo', 'telefono', 'servicioDeInteres', 'mensaje'].forEach((id) => {
      const campo = el.querySelector(`#${id}`);
      expect(campo).not.toBeNull();
      expect(el.querySelector(`label[for="${id}"]`)).not.toBeNull();
    });
  });

  it('bloquea el envio y marca el campo si el nombre esta vacio', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    expect(el.textContent).toContain('Cuéntanos tu nombre para poder saludarte bien.');
    expect(el.querySelector('#nombre')?.getAttribute('aria-invalid')).toBe('true');
  });

  it('muestra un error especifico cuando el correo tiene formato invalido', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    const correo = el.querySelector('#correo') as HTMLInputElement;
    escribir(correo, 'esto-no-es-un-correo');
    difuminar(correo);
    await fixture.whenStable();

    expect(el.textContent).toContain('Escribe un correo válido, ej. nombre@empresa.com.');
  });

  it('muestra un error especifico cuando el telefono no es un celular colombiano valido', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    const telefono = el.querySelector('#telefono') as HTMLInputElement;
    escribir(telefono, '123456');
    difuminar(telefono);
    await fixture.whenStable();

    expect(el.textContent).toContain('Escribe un número de celular colombiano válido, ej. 300 123 4567.');
  });

  it('acepta un telefono colombiano con prefijo +57 y espacios', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    const telefono = el.querySelector('#telefono') as HTMLInputElement;
    escribir(telefono, '+57 300 123 4567');
    difuminar(telefono);
    await fixture.whenStable();

    expect(el.textContent).not.toContain('celular colombiano válido');
  });

  it('bloquea el envio si no se selecciona un servicio de interes', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    expect(el.textContent).toContain('Selecciona el servicio que te interesa.');
  });

  it('bloquea el envio si el mensaje esta vacio', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    expect(el.textContent).toContain('Cuéntanos brevemente qué necesitas, así podemos ayudarte mejor.');
  });

  it('hace desaparecer el error cuando el campo corregido pasa a ser valido', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    const correo = el.querySelector('#correo') as HTMLInputElement;
    escribir(correo, 'invalido');
    difuminar(correo);
    await fixture.whenStable();
    expect(el.textContent).toContain('Escribe un correo válido, ej. nombre@empresa.com.');

    escribir(correo, 'nombre@empresa.com');
    await fixture.whenStable();

    expect(el.textContent).not.toContain('Escribe un correo válido, ej. nombre@empresa.com.');
  });

  it('la empresa es opcional: no bloquea el envio si queda vacia', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('#empresa')?.getAttribute('required')).toBeFalsy();
  });

  it('el checkbox de consentimiento no viene marcado por defecto y enlaza a la politica', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    const checkbox = el.querySelector('#aceptaConsentimiento') as HTMLInputElement;

    expect(checkbox.checked).toBe(false);
    const enlace = el.querySelector('label[for="aceptaConsentimiento"] a') as HTMLAnchorElement;
    expect(enlace).not.toBeNull();
    expect(enlace.getAttribute('href')).toContain('/legales/politica-de-datos');
  });

  it('bloquea el envio y mueve el foco al checkbox si no se acepta el consentimiento', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    escribir(el.querySelector('#nombre') as HTMLInputElement, 'Ana Pérez');
    escribir(el.querySelector('#correo') as HTMLInputElement, 'ana@empresa.com');
    escribir(el.querySelector('#telefono') as HTMLInputElement, '3001234567');
    escribir(el.querySelector('#servicioDeInteres') as HTMLSelectElement, 'OTRO');
    escribir(el.querySelector('#mensaje') as HTMLTextAreaElement, 'Necesito ayuda con mi negocio.');
    await fixture.whenStable();

    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    expect(el.textContent).toContain('Necesitamos que aceptes el tratamiento de datos para poder contactarte.');
    expect(document.activeElement?.id).toBe('aceptaConsentimiento');
  });

  it('el campo honeypot esta oculto para personas pero no interfiere con el envio', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    const honeypot = el.querySelector('#sitioWeb') as HTMLInputElement;

    expect(honeypot).not.toBeNull();
    expect(honeypot.getAttribute('aria-hidden')).toBe('true');
    expect(honeypot.getAttribute('tabindex')).toBe('-1');
    expect(honeypot.getAttribute('autocomplete')).toBe('off');
  });

  it('envia un POST a /api/solicitudes con los datos del formulario al enviar', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    llenarFormularioValido(el);
    await fixture.whenStable();

    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    const solicitud = httpMock.expectOne('/api/solicitudes');
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.body).toEqual({
      nombre: 'Ana Pérez',
      empresa: '',
      correo: 'ana@empresa.com',
      telefono: '3001234567',
      servicioDeInteres: 'OTRO',
      mensaje: 'Necesito ayuda con mi negocio.',
      aceptaConsentimiento: true,
      sitioWeb: '',
    });
    solicitud.flush({ id: '11111111-1111-1111-1111-111111111111' });
  });

  it('muestra el mensaje de confirmacion cuando la solicitud se registra con exito', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    llenarFormularioValido(el);
    await fixture.whenStable();
    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    httpMock.expectOne('/api/solicitudes').flush({ id: '11111111-1111-1111-1111-111111111111' });
    await fixture.whenStable();

    expect(el.textContent).toContain('¡Listo! Ya recibimos tu mensaje.');
    expect(el.querySelector('form')).toBeNull();
  });

  it('muestra un mensaje de error y conserva los datos si falla el envio', async () => {
    const fixture = TestBed.createComponent(ContactoPage);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    llenarFormularioValido(el);
    await fixture.whenStable();
    (el.querySelector('form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    httpMock.expectOne('/api/solicitudes').flush('Error', { status: 500, statusText: 'Internal Server Error' });
    await fixture.whenStable();

    expect(el.textContent).toContain('Algo salió mal al enviar tu mensaje. Tus datos no se perdieron');
    expect((el.querySelector('#nombre') as HTMLInputElement).value).toBe('Ana Pérez');
    expect(el.querySelector('form')).not.toBeNull();
  });
});
