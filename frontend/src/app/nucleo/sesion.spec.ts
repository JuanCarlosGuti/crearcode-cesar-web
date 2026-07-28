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

describe('SesionService', () => {
  afterEach(() => {
    sessionStorage.clear();
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
