import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { DIAGNOSTICO } from '../../../contenido/diagnostico';
import { DiagnosticoDigital } from './diagnostico-digital';

const INFORME = {
  veredicto: 'Tu negocio tiene un problema de tiempo, no de ventas.',
  oportunidades: [
    { titulo: 'Respuestas automáticas', detalle: 'Se contestan solas.', beneficio: 'Dejas de repetir.' },
    { titulo: 'Pedidos en un solo lugar', detalle: 'Todo registrado.', beneficio: 'Nadie pregunta en qué va.' },
    { titulo: 'Reportes automáticos', detalle: 'Se arman solos.', beneficio: 'Cierras sin cuadrar a mano.' },
  ],
};

async function crear() {
  const fixture = TestBed.createComponent(DiagnosticoDigital);
  await fixture.whenStable();
  return { fixture, el: fixture.nativeElement as HTMLElement };
}

async function responderTodo(fixture: Awaited<ReturnType<typeof crear>>['fixture']) {
  const el = fixture.nativeElement as HTMLElement;
  for (let i = 0; i < DIAGNOSTICO.preguntas.length; i++) {
    (el.querySelector('.diagnostico-opcion') as HTMLButtonElement).click();
    await fixture.whenStable();
  }
}

describe('DiagnosticoDigital (F10c, HU-41)', () => {
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

  it('arranca en la primera pregunta con su progreso', async () => {
    const { el } = await crear();
    expect(el.textContent).toContain(DIAGNOSTICO.preguntas[0].pregunta);
    expect(el.textContent).toContain('1 de 6');
    expect(el.querySelectorAll('.diagnostico-opcion')).toHaveLength(3);
  });

  it('responder avanza a la siguiente pregunta', async () => {
    const { fixture, el } = await crear();
    (el.querySelector('.diagnostico-opcion') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(el.textContent).toContain(DIAGNOSTICO.preguntas[1].pregunta);
    expect(el.textContent).toContain('2 de 6');
  });

  it('al responder las 6 envia los pares, muestra analizando y luego la radiografia', async () => {
    const { fixture, el } = await crear();
    await responderTodo(fixture);

    expect(el.textContent).toContain(DIAGNOSTICO.analizando);

    const solicitud = httpMock.expectOne('/api/asistente/diagnostico');
    const cuerpo = solicitud.request.body as { respuestas: { pregunta: string; respuesta: string }[] };
    expect(cuerpo.respuestas).toHaveLength(6);
    expect(cuerpo.respuestas[0]).toEqual({
      pregunta: DIAGNOSTICO.preguntas[0].pregunta,
      respuesta: DIAGNOSTICO.preguntas[0].opciones[0],
    });
    expect(solicitud.request.headers.get('X-Sesion-Anonima')).toBeTruthy();

    solicitud.flush(INFORME);
    await fixture.whenStable();

    expect(el.textContent).toContain(INFORME.veredicto);
    expect(el.querySelectorAll('.diagnostico-oportunidad')).toHaveLength(3);
    expect(el.textContent).toContain(DIAGNOSTICO.prefijoBeneficio + 'Dejas de repetir.');
    expect(el.textContent).toContain(DIAGNOSTICO.cierreTitulo);
    expect(el.querySelector('a[href="/contacto"]')).toBeTruthy();
  });

  it('el limite anonimo muestra el mensaje con CTA a crear cuenta', async () => {
    const { fixture, el } = await crear();
    await responderTodo(fixture);

    httpMock.expectOne('/api/asistente/diagnostico').flush(
      { mensaje: 'límite', codigo: 'limite-anonimo' },
      { status: 429, statusText: 'Too Many Requests' },
    );
    await fixture.whenStable();

    expect(el.textContent).toContain('los límites existen');
    expect(el.querySelector('.diagnostico-aviso a[href="/registro"]')).toBeTruthy();
  });

  it('un fallo del proveedor muestra el error amable y permite reintentar sin repetir el quiz', async () => {
    const { fixture, el } = await crear();
    await responderTodo(fixture);

    httpMock.expectOne('/api/asistente/diagnostico').flush('error', {
      status: 503,
      statusText: 'Service Unavailable',
    });
    await fixture.whenStable();

    expect(el.textContent).toContain('no te descuenta diagnósticos');

    (el.querySelector('.diagnostico-reintentar') as HTMLButtonElement).click();
    await fixture.whenStable();
    const reintento = httpMock.expectOne('/api/asistente/diagnostico');
    reintento.flush(INFORME);
    await fixture.whenStable();
    expect(el.textContent).toContain(INFORME.veredicto);
  });

  it('volver a empezar regresa a la primera pregunta', async () => {
    const { fixture, el } = await crear();
    await responderTodo(fixture);
    httpMock.expectOne('/api/asistente/diagnostico').flush(INFORME);
    await fixture.whenStable();

    (el.querySelector('.diagnostico-reiniciar') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(el.textContent).toContain(DIAGNOSTICO.preguntas[0].pregunta);
    expect(el.textContent).toContain('1 de 6');
  });
});
