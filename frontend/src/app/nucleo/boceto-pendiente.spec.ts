import { PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import {
  BocetoPendienteService,
  CLAVE_BOCETO_PENDIENTE,
  VIGENCIA_DEL_BOCETO_PENDIENTE_MS,
} from './boceto-pendiente';

const DATOS = {
  sector: 'Restaurante',
  queHace: 'Domicilios en Valledupar',
  queNecesita: 'Recibir pedidos sin saturar el WhatsApp',
};

describe('BocetoPendienteService (ISS-226)', () => {
  let servicio: BocetoPendienteService;

  beforeEach(() => {
    localStorage.removeItem(CLAVE_BOCETO_PENDIENTE);
    TestBed.configureTestingModule({});
    servicio = TestBed.inject(BocetoPendienteService);
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
    localStorage.removeItem(CLAVE_BOCETO_PENDIENTE);
  });

  it('guarda lo escrito y lo entrega una sola vez', () => {
    servicio.guardar(DATOS);

    expect(servicio.tomar()).toEqual(DATOS);
    expect(servicio.tomar()).toBeNull();
    expect(localStorage.getItem(CLAVE_BOCETO_PENDIENTE)).toBeNull();
  });

  it('preguntar si hay uno no lo consume', () => {
    servicio.guardar(DATOS);

    expect(servicio.hay()).toBe(true);
    expect(servicio.hay()).toBe(true);
    expect(servicio.tomar()).toEqual(DATOS);
    expect(servicio.hay()).toBe(false);
  });

  it('guarda sin los espacios de los extremos', () => {
    servicio.guardar({ sector: '  Ferretería ', queHace: ' Vendemos al por mayor', queNecesita: '' });

    expect(servicio.tomar()).toEqual({ sector: 'Ferretería', queHace: 'Vendemos al por mayor', queNecesita: '' });
  });

  it('vence a las 24 horas y entonces lo borra', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-10-05T12:00:00Z'));
    servicio.guardar(DATOS);

    vi.setSystemTime(new Date(Date.parse('2026-10-05T12:00:00Z') + VIGENCIA_DEL_BOCETO_PENDIENTE_MS - 1));
    expect(servicio.hay()).toBe(true);

    vi.setSystemTime(new Date(Date.parse('2026-10-05T12:00:00Z') + VIGENCIA_DEL_BOCETO_PENDIENTE_MS));
    expect(servicio.hay()).toBe(false);
    expect(servicio.tomar()).toBeNull();
    expect(localStorage.getItem(CLAVE_BOCETO_PENDIENTE)).toBeNull();
  });

  it('si no se escribió nada no guarda, y borra lo que hubiera de antes', () => {
    servicio.guardar(DATOS);
    servicio.guardar({ sector: ' ', queHace: '', queNecesita: '' });

    expect(servicio.hay()).toBe(false);
    expect(localStorage.getItem(CLAVE_BOCETO_PENDIENTE)).toBeNull();
  });

  it('ignora lo que no pueda leer: JSON roto o con otra forma', () => {
    localStorage.setItem(CLAVE_BOCETO_PENDIENTE, '{roto');
    expect(servicio.hay()).toBe(false);
    expect(servicio.tomar()).toBeNull();

    localStorage.setItem(
      CLAVE_BOCETO_PENDIENTE,
      JSON.stringify({ sector: 42, queHace: 'x', queNecesita: 'y', guardadoEn: Date.now() }),
    );
    expect(servicio.tomar()).toBeNull();
  });

  it('si el navegador bloquea el almacenamiento, sigue sin romper nada', () => {
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('bloqueado');
    });
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('bloqueado');
    });

    expect(() => servicio.guardar(DATOS)).not.toThrow();
    expect(servicio.hay()).toBe(false);
    expect(servicio.tomar()).toBeNull();
  });

  it('en el servidor (prerender) no toca el almacenamiento', () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ providers: [{ provide: PLATFORM_ID, useValue: 'server' }] });
    const enServidor = TestBed.inject(BocetoPendienteService);
    const escritura = vi.spyOn(Storage.prototype, 'setItem');

    enServidor.guardar(DATOS);

    expect(escritura).not.toHaveBeenCalled();
    expect(enServidor.hay()).toBe(false);
  });
});
