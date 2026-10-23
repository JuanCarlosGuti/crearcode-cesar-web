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
    localStorage.clear();
    vi.restoreAllMocks();
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
    localStorage.setItem(
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

  // Las sesiones de cliente de antes de ISS-240 viven en sessionStorage:
  // se siguen leyendo para que nadie quede afuera el dia del despliegue.
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

  /**
   * La sesion del cliente era de cada pestana: «Ver demo», el enlace de
   * un correo o «abrir en pestana nueva» la perdian, y el sitio le
   * volvia a pedir crear cuenta a quien ya la tenia (ISS-240, ADR-08
   * enmendado el 23 oct 2026). La del admin sigue siendo de su pestana.
   */
  describe('entre pestanas (ISS-240)', () => {
    function sesionDelClienteEnOtraPestana(token = tokenQueVenceEn(60_000)): string {
      const datos = JSON.stringify({ ...SESION_CLIENTE, token });
      localStorage.setItem('crearcode-sesion', datos);
      return datos;
    }

    function avisarCambioDeOtraPestana(nuevoValor: string | null): void {
      window.dispatchEvent(new StorageEvent('storage', { key: 'crearcode-sesion', newValue: nuevoValor }));
    }

    it('la sesion del cliente queda en localStorage, que comparten todas las pestanas', () => {
      const sesion = TestBed.inject(SesionService);

      sesion.iniciarSesion(SESION_CLIENTE);

      expect(JSON.parse(localStorage.getItem('crearcode-sesion')!)).toEqual(SESION_CLIENTE);
      expect(sessionStorage.getItem('crearcode-sesion')).toBeNull();
    });

    it('una pestana nueva encuentra abierta la sesion del cliente', () => {
      sesionDelClienteEnOtraPestana();

      const sesion = TestBed.inject(SesionService);

      expect(sesion.esCliente()).toBe(true);
    });

    it('la sesion del admin sigue siendo solo de su pestana', () => {
      const sesion = TestBed.inject(SesionService);

      sesion.iniciarSesion(SESION_ADMIN);

      expect(JSON.parse(sessionStorage.getItem('crearcode-sesion')!)).toEqual(SESION_ADMIN);
      expect(localStorage.getItem('crearcode-sesion')).toBeNull();
    });

    it('cerrar sesion en otra pestana la cierra tambien en esta', () => {
      const sesion = TestBed.inject(SesionService);
      sesion.iniciarSesion(SESION_CLIENTE);

      localStorage.removeItem('crearcode-sesion');
      avisarCambioDeOtraPestana(null);

      expect(sesion.estaAutenticado()).toBe(false);
    });

    it('ingresar en otra pestana abre la sesion tambien en esta', () => {
      const sesion = TestBed.inject(SesionService);

      avisarCambioDeOtraPestana(sesionDelClienteEnOtraPestana());

      expect(sesion.esCliente()).toBe(true);
      expect(sesion.correo()).toBe(SESION_CLIENTE.correo);
    });

    it('lo que pase con el cliente en otra pestana no toca al admin de esta', () => {
      const sesion = TestBed.inject(SesionService);
      sesion.iniciarSesion(SESION_ADMIN);

      avisarCambioDeOtraPestana(sesionDelClienteEnOtraPestana());

      expect(sesion.esAdmin()).toBe(true);
    });

    it('cerrar la sesion del admin no saca al cliente de las otras pestanas', () => {
      const datosDelCliente = sesionDelClienteEnOtraPestana();
      const sesion = TestBed.inject(SesionService);
      sesion.iniciarSesion(SESION_ADMIN);

      sesion.cerrarSesion();

      expect(sessionStorage.getItem('crearcode-sesion')).toBeNull();
      expect(localStorage.getItem('crearcode-sesion')).toBe(datosDelCliente);
    });

    it('cerrar la sesion del cliente la borra de todas las pestanas', () => {
      const sesion = TestBed.inject(SesionService);
      sesion.iniciarSesion(SESION_CLIENTE);

      sesion.cerrarSesion();

      expect(localStorage.getItem('crearcode-sesion')).toBeNull();
    });

    it('una sesion que llega vencida desde otra pestana no se abre', () => {
      const sesion = TestBed.inject(SesionService);

      avisarCambioDeOtraPestana(sesionDelClienteEnOtraPestana(tokenQueVenceEn(-1000)));

      expect(sesion.estaAutenticado()).toBe(false);
    });

    it('la sesion que llega de otra pestana tambien se cierra sola al vencer', () => {
      vi.useFakeTimers();
      try {
        const sesion = TestBed.inject(SesionService);
        avisarCambioDeOtraPestana(sesionDelClienteEnOtraPestana(tokenQueVenceEn(60_000)));
        expect(sesion.estaAutenticado()).toBe(true);

        vi.advanceTimersByTime(61_000);

        expect(sesion.estaAutenticado()).toBe(false);
      } finally {
        vi.useRealTimers();
      }
    });

    it('si el navegador bloquea el almacenamiento, la sesion sigue en memoria sin romper', () => {
      vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
        throw new Error('bloqueado');
      });
      const sesion = TestBed.inject(SesionService);

      expect(() => sesion.iniciarSesion(SESION_CLIENTE)).not.toThrow();
      expect(sesion.esCliente()).toBe(true);
    });
  });
});
