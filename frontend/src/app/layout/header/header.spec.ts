import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { SERVICIOS } from '../../../contenido/servicios';
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

  it('incluye el CTA doble: agendar consulta y WhatsApp', async () => {
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('a[href="/contacto"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('a[href^="https://wa.me/"]')).toBeTruthy();
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

  it('usa el mensaje de WhatsApp propio del servicio al navegar a su pagina', async () => {
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();

    const servicio = SERVICIOS[1];
    await TestBed.inject(Router).navigateByUrl(`/servicios/${servicio.slug}`);
    await fixture.whenStable();

    const enlace = fixture.nativeElement.querySelector('a[href^="https://wa.me/"]') as HTMLAnchorElement;
    expect(enlace.href).toContain(encodeURIComponent(servicio.mensajeWhatsapp));
  });
});
