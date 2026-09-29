import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { App } from './app';

@Component({ template: '' })
class PaginaVaciaDePrueba {}

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([{ path: 'admin/login', component: PaginaVaciaDePrueba }])],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  /**
   * Con el header actual (marca, boton de menu, siete enlaces, el
   * desplegable de Servicios y doble CTA) un usuario de teclado tabula
   * una docena de veces antes de llegar al contenido, en cada pagina.
   * axe no lo delata porque la regla 'bypass' se satisface con el
   * landmark <main> (auditoria del 28 sep 2026, P2-5a).
   */
  it('el primer elemento enfocable es el salto al contenido, y apunta al main', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    const salto = el.querySelector('a.salto-al-contenido') as HTMLAnchorElement;
    expect(salto).toBeTruthy();
    expect(el.firstElementChild).toBe(salto);
    expect(salto.getAttribute('href')).toBe('#contenido');
    expect(el.querySelector('main')?.id).toBe('contenido');
  });

  /**
   * En movil el WhatsApp estaba escondido tras el menu hamburguesa o
   * al final del pie (auditoria P1-4). El CSS lo oculta desde 60rem,
   * el mismo corte con el que el header se pliega.
   */
  it('renderiza el boton flotante de WhatsApp con destino y etiqueta accesible', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    const flotante = el.querySelector('app-whatsapp-flotante a') as HTMLAnchorElement;
    expect(flotante.href).toContain('https://wa.me/');
    expect(flotante.getAttribute('aria-label')).toBe('Escríbenos por WhatsApp');
    expect(flotante.getAttribute('rel')).toContain('noopener');
  });

  it('renderiza el header y el footer', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-header')).toBeTruthy();
    expect(compiled.querySelector('app-footer')).toBeTruthy();
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
  });

  it('no renderiza el header ni el footer publicos en rutas del panel admin', async () => {
    const fixture = TestBed.createComponent(App);
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/admin/login');
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-header')).toBeNull();
    expect(compiled.querySelector('app-footer')).toBeNull();
  });
});
