import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Header } from './header';

describe('Header', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([])],
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
});
