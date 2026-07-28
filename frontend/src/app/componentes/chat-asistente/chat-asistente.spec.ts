import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { ChatAsistente } from './chat-asistente';

async function crearWidget() {
  const fixture = TestBed.createComponent(ChatAsistente);
  await fixture.whenStable();
  return { fixture, el: fixture.nativeElement as HTMLElement };
}

async function abrirPanel(fixture: Awaited<ReturnType<typeof crearWidget>>['fixture']) {
  const el = fixture.nativeElement as HTMLElement;
  (el.querySelector('.chat-burbuja') as HTMLButtonElement).click();
  await fixture.whenStable();
}

function escribirYEnviar(el: HTMLElement, texto: string) {
  const input = el.querySelector('.chat-panel input[type="text"]') as HTMLInputElement;
  input.value = texto;
  input.dispatchEvent(new Event('input'));
  (el.querySelector('.chat-panel form') as HTMLFormElement).requestSubmit();
}

describe('ChatAsistente', () => {
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

  it('la burbuja abre el panel y el boton de cerrar lo cierra', async () => {
    const { fixture, el } = await crearWidget();
    const burbuja = el.querySelector('.chat-burbuja') as HTMLButtonElement;
    expect(burbuja.getAttribute('aria-expanded')).toBe('false');
    expect(el.querySelector('.chat-panel')).toBeNull();

    await abrirPanel(fixture);
    expect(burbuja.getAttribute('aria-expanded')).toBe('true');
    expect(el.querySelector('.chat-panel')).not.toBeNull();
    expect(el.textContent).toContain('Asistente de Crear Code Cesar');

    (el.querySelector('.chat-panel__cerrar') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(el.querySelector('.chat-panel')).toBeNull();
  });

  it('la tecla Escape cierra el panel', async () => {
    const { fixture, el } = await crearWidget();
    await abrirPanel(fixture);

    el.querySelector('.chat-panel')!.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    await fixture.whenStable();

    expect(el.querySelector('.chat-panel')).toBeNull();
  });

  it('sin mensajes muestra las sugerencias y elegir una envia la pregunta', async () => {
    const { fixture, el } = await crearWidget();
    await abrirPanel(fixture);

    const sugerencias = el.querySelectorAll('.chat-sugerencia');
    expect(sugerencias.length).toBe(3);

    (sugerencias[0] as HTMLButtonElement).click();
    await fixture.whenStable();

    const solicitud = httpMock.expectOne('/api/asistente/mensajes');
    expect((solicitud.request.body as { mensajes: { texto: string }[] }).mensajes[0].texto).toBe(
      '¿Qué servicios ofrecen?',
    );
    solicitud.flush({ texto: 'Te cuento.', escalarAHumano: false });
    await fixture.whenStable();

    expect(el.querySelectorAll('.chat-sugerencia').length).toBe(0);
    expect(el.textContent).toContain('Te cuento.');
  });

  it('enviar desde el formulario muestra la pregunta y la respuesta en la lista', async () => {
    const { fixture, el } = await crearWidget();
    await abrirPanel(fixture);

    escribirYEnviar(el, '¿Cómo trabajan?');
    await fixture.whenStable();

    expect(el.textContent).toContain('¿Cómo trabajan?');
    expect(el.textContent).toContain('El asistente está escribiendo…');

    httpMock.expectOne('/api/asistente/mensajes').flush({
      texto: 'Documentar, probar, construir.',
      escalarAHumano: false,
    });
    await fixture.whenStable();

    expect(el.textContent).toContain('Documentar, probar, construir.');
    expect(el.textContent).not.toContain('El asistente está escribiendo…');
    expect((el.querySelector('.chat-panel input[type="text"]') as HTMLInputElement).value).toBe('');
  });

  it('la lista de mensajes es una region viva para lectores de pantalla', async () => {
    const { fixture, el } = await crearWidget();
    await abrirPanel(fixture);

    expect(el.querySelector('.chat-mensajes')?.getAttribute('aria-live')).toBe('polite');
  });
});
