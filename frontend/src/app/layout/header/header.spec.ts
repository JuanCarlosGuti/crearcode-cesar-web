import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { SesionService } from '../../nucleo/sesion';
import { Header } from './header';

@Component({ template: '' })
class PaginaVaciaDePrueba {}

describe('Header', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: 'servicios/:slug', component: PaginaVaciaDePrueba },
          { path: '**', component: PaginaVaciaDePrueba },
        ]),
      ],
    });
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('muestra el nombre de la empresa', async () => {
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Crear Code Cesar');
  });

  it('incluye un enlace de navegacion por cada servicio', async () => {
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();

    const enlaces = fixture.nativeElement.querySelectorAll('a[href^="/servicios/"]');

    expect(enlaces.length).toBe(3);
  });

  it('incluye el CTA doble del prototipo: agenda y crear cuenta (ISS-135)', async () => {
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();
    const cta = fixture.nativeElement.querySelector('.cabecera__cta') as HTMLElement;

    expect(cta.querySelector('a[href="/contacto"]')?.textContent).toContain('Agenda tu consulta');
    expect(cta.querySelector('a[href="/registro"]')?.textContent).toContain('Crear cuenta');
    expect(fixture.nativeElement.querySelector('a[href^="https://wa.me/"]')).toBeNull();
  });

  it('alterna el menu movil al hacer click en el boton', async () => {
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();
    const boton = fixture.nativeElement.querySelector('.cabecera__boton-menu') as HTMLButtonElement;
    expect(boton.getAttribute('aria-expanded')).toBe('false');

    boton.click();
    await fixture.whenStable();

    expect(boton.getAttribute('aria-expanded')).toBe('true');
  });

  it('con sesion de cliente el CTA de cuenta es Mi cuenta y no duplica el enlace en el nav', async () => {
    TestBed.inject(SesionService).iniciarSesion({
      token: 'token-cliente',
      rol: 'CLIENTE',
      correo: 'cliente@correo-de-prueba.com',
    });
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelectorAll('a[href="/mi-cuenta"]').length).toBe(1);
    expect(el.querySelector('.cabecera__cta a[href="/registro"]')).toBeNull();
  });

  it('sin sesion muestra el enlace Ingresar', async () => {
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();

    const enlace = fixture.nativeElement.querySelector('a[href="/ingreso"]') as HTMLAnchorElement;
    expect(enlace).not.toBeNull();
    expect(enlace.textContent).toContain('Ingresar');
  });

  it('con sesion de cliente muestra Mi cuenta apuntando a /mi-cuenta', async () => {
    TestBed.inject(SesionService).iniciarSesion({
      token: 'token-cliente',
      rol: 'CLIENTE',
      correo: 'cliente@correo-de-prueba.com',
    });
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();

    const enlace = fixture.nativeElement.querySelector('a[href="/mi-cuenta"]') as HTMLAnchorElement;
    expect(enlace).not.toBeNull();
    expect(enlace.textContent).toContain('Mi cuenta');
    expect(fixture.nativeElement.querySelector('a[href="/ingreso"]')).toBeNull();
  });

  it('con sesion de admin el enlace de cuenta apunta al panel /admin', async () => {
    TestBed.inject(SesionService).iniciarSesion({
      token: 'token-admin',
      rol: 'ADMIN',
      correo: 'admin@crearcode-cesar.local',
    });
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('a[href="/admin"]')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('a[href="/ingreso"]')).toBeNull();
  });
});
