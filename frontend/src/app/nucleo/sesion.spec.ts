import { vi } from 'vitest';
import { PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { DatosDeSesion, SesionService } from './sesion';

const SESION_ADMIN: DatosDeSesion = {
  token: 'token-admin',
  rol: 'ADMIN',
  correo: 'admin@crearcode-cesar.local',
};

const SESION_CLIENTE: DatosDeSesion = {
  token: 'token-cliente',
  rol: 'CLIENTE',
  correo: 'cliente@correo-de-prueba.com',
};

/**
 * JWT de mentira con la forma correcta: solo interesa su `exp`. En
 * segundos enteros, como manda el estandar, redondeando hacia arriba
 * para que un token "de 60 s" no venza antes por el redondeo.
 */
function tokenQueVenceEn(milisegundosDesdeAhora: number): string {
  const carga = btoa(JSON.stringify({ exp: Math.ceil((Date.now() + milisegundosDesdeAhora) / 1000) }));
  return `cabecera.${carga}.firma`;
}

describe('SesionService', () => {
  afterEach(() => {
    sessionStorage.clear();
  });

  /**
   * Una pestana abierta con el token ya vencido seguia pintando "Mi
   * cuenta", pasaba los guards y mandaba el Bearer muerto: el
   * visitante solo se enteraba cuando algo fallaba con un 401
   * (auditoria del 28 sep 2026, P2-7).
   */
  it('no restaura una sesion cuyo token ya vencio', () => {
    sessionStorage.setItem(
      'crearcode-sesion',
      JSON.stringify({ ...SESION_ADMIN, token: tokenQueVenceEn(-1000) }),
    );

    const sesion = TestBed.inject(SesionService);

    expect(sesion.estaAutenticado()).toBe(false);
    expect(sessionStorage.getItem('crearcode-sesion')).toBeNull();
  });

  it('si restaura una sesion cuyo token sigue vigente', () => {
    sessionStorage.setItem(
      'crearcode-sesion',
      JSON.stringify({ ...SESION_ADMIN, token: tokenQueVenceEn(60_000) }),
    );

    const sesion = TestBed.inject(SesionService);

    expect(sesion.estaAutenticado()).toBe(true);
  });

  it('no entrega un token vencido y cierra la sesion al pedirlo', () => {
    // Se escribe directo en el almacenamiento: iniciarSesion con un
    // token ya vencido lo cerraria de inmediato y no probaria esto.
    const sesion = TestBed.inject(SesionService);
    sesion.iniciarSesion({ ...SESION_CLIENTE, token: tokenQueVenceEn(60_000) });
    sessionStorage.setItem(
      'crearcode-sesion',
      JSON.stringify({ ...SESION_CLIENTE, token: tokenQueVenceEn(-60_000) }),
    );
    TestBed.resetTestingModule();

    const enLaSiguienteCarga = TestBed.inject(SesionService);

    expect(enLaSiguienteCarga.obtenerToken()).toBeNull();
    expect(enLaSiguienteCarga.estaAutenticado()).toBe(false);
  });

  /**
   * Sin esto el cierre era solo reactivo —el interceptor limpiaba al
   * recibir un 401—, asi que entre el vencimiento y la siguiente
   * peticion la interfaz aseguraba una sesion que ya no existia.
   */
  it('cierra la sesion sola al vencer, sin que nadie haga nada', () => {
    vi.useFakeTimers();
    try {
      const sesion = TestBed.inject(SesionService);
      sesion.iniciarSesion({ ...SESION_CLIENTE, token: tokenQueVenceEn(60_000) });
      expect(sesion.estaAutenticado()).toBe(true);

      vi.advanceTimersByTime(61_000);

      expect(sesion.estaAutenticado()).toBe(false);
    } finally {
      vi.useRealTimers();
    }
  });

  it('empieza sin sesion si no hay nada guardado', () => {
    const sesion = TestBed.inject(SesionService);

    expect(sesion.estaAutenticado()).toBe(false);
    expect(sesion.obtenerToken()).toBeNull();
    expect(sesion.rol()).toBeNull();
    expect(sesion.correo()).toBeNull();
  });

  it('iniciarSesion guarda token, rol y correo, y marca autenticado', () => {
    const sesion = TestBed.inject(SesionService);

    sesion.iniciarSesion(SESION_ADMIN);

    expect(sesion.estaAutenticado()).toBe(true);
    expect(sesion.obtenerToken()).toBe('token-admin');
    expect(sesion.rol()).toBe('ADMIN');
    expect(sesion.correo()).toBe('admin@crearcode-cesar.local');
    expect(JSON.parse(sessionStorage.getItem('crearcode-sesion')!)).toEqual(SESION_ADMIN);
  });

  it('una sesion de admin es esAdmin y no esCliente', () => {
    const sesion = TestBed.inject(SesionService);

    sesion.iniciarSesion(SESION_ADMIN);

    expect(sesion.esAdmin()).toBe(true);
    expect(sesion.esCliente()).toBe(false);
  });

  it('una sesion de cliente es esCliente y no esAdmin', () => {
    const sesion = TestBed.inject(SesionService);

    sesion.iniciarSesion(SESION_CLIENTE);

    expect(sesion.esCliente()).toBe(true);
    expect(sesion.esAdmin()).toBe(false);
  });

  it('cerrarSesion limpia la sesion completa', () => {
    const sesion = TestBed.inject(SesionService);
    sesion.iniciarSesion(SESION_ADMIN);

    sesion.cerrarSesion();

    expect(sesion.estaAutenticado()).toBe(false);
    expect(sesion.esAdmin()).toBe(false);
    expect(sessionStorage.getItem('crearcode-sesion')).toBeNull();
  });

  it('recupera una sesion ya guardada en sessionStorage al crearse (ej. tras recargar la pagina)', () => {
    sessionStorage.setItem('crearcode-sesion', JSON.stringify(SESION_CLIENTE));

    const sesion = TestBed.inject(SesionService);

    expect(sesion.estaAutenticado()).toBe(true);
    expect(sesion.obtenerToken()).toBe('token-cliente');
    expect(sesion.esCliente()).toBe(true);
  });

  it('ignora contenido corrupto en sessionStorage en vez de romper', () => {
    sessionStorage.setItem('crearcode-sesion', 'esto-no-es-json{');

    const sesion = TestBed.inject(SesionService);

    expect(sesion.estaAutenticado()).toBe(false);
  });

  it('no accede a sessionStorage fuera del navegador (SSR)', () => {
    TestBed.overrideProvider(PLATFORM_ID, { useValue: 'server' });

    expect(() => {
      const sesion = TestBed.inject(SesionService);
      expect(sesion.estaAutenticado()).toBe(false);
      sesion.iniciarSesion(SESION_ADMIN);
      expect(sesion.obtenerToken()).toBe('token-admin');
    }).not.toThrow();
  });
});
