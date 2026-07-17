import { PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { SesionService } from './sesion';

describe('SesionService', () => {
  afterEach(() => {
    sessionStorage.clear();
  });

  it('empieza sin sesion si no hay token guardado', () => {
    const sesion = TestBed.inject(SesionService);

    expect(sesion.estaAutenticado()).toBe(false);
    expect(sesion.obtenerToken()).toBeNull();
  });

  it('iniciarSesion guarda el token y marca autenticado', () => {
    const sesion = TestBed.inject(SesionService);

    sesion.iniciarSesion('token-de-prueba');

    expect(sesion.estaAutenticado()).toBe(true);
    expect(sesion.obtenerToken()).toBe('token-de-prueba');
    expect(sessionStorage.getItem('crearcode-admin-token')).toBe('token-de-prueba');
  });

  it('cerrarSesion limpia el token', () => {
    const sesion = TestBed.inject(SesionService);
    sesion.iniciarSesion('token-de-prueba');

    sesion.cerrarSesion();

    expect(sesion.estaAutenticado()).toBe(false);
    expect(sessionStorage.getItem('crearcode-admin-token')).toBeNull();
  });

  it('recupera un token ya guardado en sessionStorage al crearse (ej. tras recargar la pagina)', () => {
    sessionStorage.setItem('crearcode-admin-token', 'token-existente');

    const sesion = TestBed.inject(SesionService);

    expect(sesion.estaAutenticado()).toBe(true);
    expect(sesion.obtenerToken()).toBe('token-existente');
  });

  it('no accede a sessionStorage fuera del navegador (SSR)', () => {
    TestBed.overrideProvider(PLATFORM_ID, { useValue: 'server' });

    expect(() => {
      const sesion = TestBed.inject(SesionService);
      expect(sesion.estaAutenticado()).toBe(false);
      sesion.iniciarSesion('token-de-prueba');
      expect(sesion.obtenerToken()).toBe('token-de-prueba');
    }).not.toThrow();
  });
});
