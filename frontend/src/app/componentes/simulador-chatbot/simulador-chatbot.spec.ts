import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { SIMULADOR } from '../../../contenido/simulador';
import { SimuladorChatbot } from './simulador-chatbot';

async function crear() {
  const fixture = TestBed.createComponent(SimuladorChatbot);
  await fixture.whenStable();
  return { fixture, el: fixture.nativeElement as HTMLElement };
}

function escribir(el: HTMLElement, selector: string, valor: string) {
  const campo = el.querySelector<HTMLInputElement>(selector)!;
  campo.value = valor;
  campo.dispatchEvent(new Event('input'));
}

async function prepararNegocioYEnviar(
  fixture: Awaited<ReturnType<typeof crear>>['fixture'],
  texto: string,
) {
  const el = fixture.nativeElement as HTMLElement;
  escribir(el, '#simulador-nombre', 'Ferretería La 16');
  escribir(el, '#simulador-rubro', 'ferretería');
  escribir(el, '#simulador-mensaje', texto);
  await fixture.whenStable();
  (el.querySelector('.simulador-formulario button[type="submit"]') as HTMLButtonElement).click();
  await fixture.whenStable();
}

describe('SimuladorChatbot (F10b, HU-40)', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('muestra el formulario del negocio, el saludo inicial y las notas de honestidad', async () => {
    const { el } = await crear();
    expect(el.querySelector('#simulador-nombre')).toBeTruthy();
    expect(el.querySelector('#simulador-rubro')).toBeTruthy();
    expect(el.textContent).toContain(SIMULADOR.mensajeInicial);
    expect(el.textContent).toContain(SIMULADOR.notaDemo);
    expect(el.textContent).toContain(SIMULADOR.notaIa);
  });

  it('sin nombre y rubro no llama a la API y muestra el aviso', async () => {
    const { fixture, el } = await crear();
    escribir(el, '#simulador-mensaje', 'hola');
    await fixture.whenStable();
    (el.querySelector('.simulador-formulario button[type="submit"]') as HTMLButtonElement).click();
    await fixture.whenStable();

    httpMock.expectNone('/api/asistente/simulador');
    expect(el.textContent).toContain(SIMULADOR.avisoNegocioIncompleto);
  });

  it('con negocio completo envia la peticion correcta y muestra la respuesta', async () => {
    const { fixture, el } = await crear();
    await prepararNegocioYEnviar(fixture, '¿Tienen tornillos?');

    const solicitud = httpMock.expectOne('/api/asistente/simulador');
    const cuerpo = solicitud.request.body as {
      negocio: { nombre: string; rubro: string };
      mensajes: { rol: string; texto: string }[];
    };
    expect(cuerpo.negocio).toEqual({ nombre: 'Ferretería La 16', rubro: 'ferretería' });
    expect(cuerpo.mensajes.at(-1)).toEqual({ rol: 'USUARIO', texto: '¿Tienen tornillos?' });
    expect(solicitud.request.headers.get('X-Sesion-Anonima')).toBeTruthy();

    solicitud.flush({ texto: 'Con gusto te confirmo disponibilidad.', escalarAHumano: false });
    await fixture.whenStable();

    expect(el.textContent).toContain('Con gusto te confirmo disponibilidad.');
    expect(el.textContent).toContain('Ferretería La 16' + SIMULADOR.sufijoTituloChat);
  });

  it('el limite anonimo muestra el mensaje y el CTA a crear cuenta', async () => {
    const { fixture, el } = await crear();
    await prepararNegocioYEnviar(fixture, 'hola');

    httpMock.expectOne('/api/asistente/simulador').flush(
      { mensaje: 'límite', codigo: 'limite-anonimo' },
      { status: 429, statusText: 'Too Many Requests' },
    );
    await fixture.whenStable();

    expect(el.textContent).toContain('los límites existen');
    expect(el.querySelector('.simulador-aviso a[href="/registro"]')).toBeTruthy();
  });

  it('un fallo del proveedor muestra el error amable sin perder la conversacion', async () => {
    const { fixture, el } = await crear();
    await prepararNegocioYEnviar(fixture, 'hola');

    httpMock.expectOne('/api/asistente/simulador').flush('error', {
      status: 503,
      statusText: 'Service Unavailable',
    });
    await fixture.whenStable();

    expect(el.textContent).toContain('No perdiste ningún mensaje');
    expect(el.textContent).toContain('hola');
  });
});
