import { TestBed } from '@angular/core/testing';

import { AsistenteUiService } from './asistente-ui';

describe('AsistenteUiService', () => {
  let servicio: AsistenteUiService;

  beforeEach(() => {
    servicio = TestBed.inject(AsistenteUiService);
  });

  it('arranca sin aperturas ni pregunta pendiente', () => {
    expect(servicio.aperturas()).toBe(0);
    expect(servicio.consumirPregunta()).toBeNull();
  });

  it('abrir incrementa el contador aunque se repita la misma pregunta', () => {
    servicio.abrir('¿Qué servicios ofrecen?');
    servicio.abrir('¿Qué servicios ofrecen?');

    expect(servicio.aperturas()).toBe(2);
  });

  it('la pregunta se consume una sola vez', () => {
    servicio.abrir('¿Cómo trabajan un proyecto?');

    expect(servicio.consumirPregunta()).toBe('¿Cómo trabajan un proyecto?');
    expect(servicio.consumirPregunta()).toBeNull();
  });

  it('abrir sin pregunta limpia cualquier pregunta anterior no consumida', () => {
    servicio.abrir('¿Cuánto tarda un desarrollo?');
    servicio.abrir();

    expect(servicio.consumirPregunta()).toBeNull();
  });
});
