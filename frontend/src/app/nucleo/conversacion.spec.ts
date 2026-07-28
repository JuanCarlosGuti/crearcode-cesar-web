import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ConversacionService } from './conversacion';

describe('ConversacionService', () => {
  let servicio: ConversacionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(ConversacionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('enviar agrega la pregunta y luego la respuesta del asistente', () => {
    servicio.enviar('¿Qué servicios ofrecen?');

    expect(servicio.mensajes()).toHaveLength(1);
    expect(servicio.mensajes()[0]).toEqual({ rol: 'USUARIO', texto: '¿Qué servicios ofrecen?' });
    expect(servicio.enviando()).toBe(true);

    httpMock.expectOne('/api/asistente/mensajes').flush({ texto: 'Tres líneas de servicio.', escalarAHumano: false });

    expect(servicio.enviando()).toBe(false);
    expect(servicio.mensajes()[1]).toEqual({
      rol: 'ASISTENTE',
      texto: 'Tres líneas de servicio.',
      escalar: false,
    });
  });

  it('marca el escalamiento cuando la respuesta lo trae', () => {
    servicio.enviar('¿Cuánto cuesta una app?');
    httpMock.expectOne('/api/asistente/mensajes').flush({ texto: 'Se cotiza a la medida.', escalarAHumano: true });

    expect(servicio.mensajes()[1].escalar).toBe(true);
  });

  it('ignora un envio vacio o mientras ya hay uno en curso', () => {
    servicio.enviar('   ');
    httpMock.expectNone('/api/asistente/mensajes');

    servicio.enviar('primera');
    servicio.enviar('segunda mientras responde');
    const solicitudes = httpMock.match('/api/asistente/mensajes');
    expect(solicitudes.length).toBe(1);
    solicitudes[0].flush({ texto: 'ok', escalarAHumano: false });
  });

  it('un 429 de limite anonimo deja el codigo en la senal de error y conserva la pregunta', () => {
    servicio.enviar('hola');
    httpMock.expectOne('/api/asistente/mensajes').flush(
      { mensaje: 'límite', codigo: 'limite-anonimo' },
      { status: 429, statusText: 'Too Many Requests' },
    );

    expect(servicio.error()).toBe('limite-anonimo');
    expect(servicio.mensajes()).toEqual([{ rol: 'USUARIO', texto: 'hola' }]);
    expect(servicio.enviando()).toBe(false);
  });

  it('un fallo del proveedor o de red queda como no-disponible', () => {
    servicio.enviar('hola');
    httpMock.expectOne('/api/asistente/mensajes').flush('error', {
      status: 503,
      statusText: 'Service Unavailable',
    });

    expect(servicio.error()).toBe('no-disponible');
  });

  it('un envio nuevo limpia el error anterior', () => {
    servicio.enviar('hola');
    httpMock.expectOne('/api/asistente/mensajes').flush('error', {
      status: 503,
      statusText: 'Service Unavailable',
    });

    servicio.enviar('otra pregunta');
    expect(servicio.error()).toBeNull();
    httpMock.expectOne('/api/asistente/mensajes').flush({ texto: 'ok', escalarAHumano: false });
  });

  it('reutiliza el mismo id de sesion anonima entre envios', () => {
    servicio.enviar('uno');
    const primera = httpMock.expectOne('/api/asistente/mensajes');
    primera.flush({ texto: 'ok', escalarAHumano: false });

    servicio.enviar('dos');
    const segunda = httpMock.expectOne('/api/asistente/mensajes');
    segunda.flush({ texto: 'ok', escalarAHumano: false });

    const id = primera.request.headers.get('X-Sesion-Anonima');
    expect(id).not.toBeNull();
    expect(segunda.request.headers.get('X-Sesion-Anonima')).toBe(id);
  });
});
