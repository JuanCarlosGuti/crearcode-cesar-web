import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { SERVICIOS } from '../../../contenido/servicios';
import { HOME } from '../../../contenido/home';
import { Footer } from './footer';

@Component({ template: '' })
class PaginaVaciaDePrueba {}

describe('Footer', () => {
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

  it('muestra los datos de contacto de la empresa', async () => {
    const fixture = TestBed.createComponent(Footer);
    await fixture.whenStable();

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Valledupar');
    expect(texto).toContain('323 988 5883');
    expect(texto).toContain('crearcodecesar@gmail.com');
  });

  it('incluye los enlaces legales', async () => {
    const fixture = TestBed.createComponent(Footer);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('a[href="/legales/politica-de-datos"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('a[href="/legales/terminos"]')).toBeTruthy();
  });

  it('muestra el anio actual en el copyright', async () => {
    const fixture = TestBed.createComponent(Footer);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain(String(new Date().getFullYear()));
  });

  it('usa el mensaje generico de WhatsApp fuera de una pagina de servicio', async () => {
    const fixture = TestBed.createComponent(Footer);
    await fixture.whenStable();

    const enlace = fixture.nativeElement.querySelector('a[href^="https://wa.me/"]') as HTMLAnchorElement;
    expect(enlace.href).toContain(encodeURIComponent(HOME.mensajeWhatsapp));
  });

  it('usa el mensaje de WhatsApp propio del servicio al navegar a su pagina', async () => {
    const fixture = TestBed.createComponent(Footer);
    await fixture.whenStable();

    const servicio = SERVICIOS[1];
    await TestBed.inject(Router).navigateByUrl(`/servicios/${servicio.slug}`);
    await fixture.whenStable();

    const enlace = fixture.nativeElement.querySelector('a[href^="https://wa.me/"]') as HTMLAnchorElement;
    expect(enlace.href).toContain(encodeURIComponent(servicio.mensajeWhatsapp));
  });
});
