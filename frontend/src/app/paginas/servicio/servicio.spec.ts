import { TestBed } from '@angular/core/testing';
import { Meta, Title } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';

import { SERVICIO_ASIDE, SERVICIOS } from '../../../contenido/servicios';
import { ServicioPage } from './servicio';

describe('ServicioPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('muestra la estructura completa del servicio dado su slug', async () => {
    const servicio = SERVICIOS[0];
    const fixture = TestBed.createComponent(ServicioPage);
    fixture.componentRef.setInput('slug', servicio.slug);
    await fixture.whenStable();

    const texto = fixture.nativeElement.textContent;
    expect(fixture.nativeElement.querySelector('h1')?.textContent).toBe(servicio.nombre);
    expect(texto).toContain(servicio.problema);
    servicio.incluye.forEach((item) => expect(texto).toContain(item));
    servicio.entregables.forEach((item) => expect(texto).toContain(item));
    servicio.proceso.forEach((paso) => expect(texto).toContain(paso.titulo));
    servicio.faq.forEach((pregunta) => expect(texto).toContain(pregunta.pregunta));
  });

  it('muestra un mensaje claro cuando el slug no corresponde a ningun servicio', async () => {
    const fixture = TestBed.createComponent(ServicioPage);
    fixture.componentRef.setInput('slug', 'no-existe');
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Servicio no encontrado');
  });

  it('incluye el CTA doble con el mensaje de WhatsApp propio del servicio', async () => {
    const servicio = SERVICIOS[0];
    const fixture = TestBed.createComponent(ServicioPage);
    fixture.componentRef.setInput('slug', servicio.slug);
    await fixture.whenStable();

    const enlaceWhatsapp = fixture.nativeElement.querySelector('a[href^="https://wa.me/"]') as HTMLAnchorElement;
    expect(enlaceWhatsapp.href).toContain(encodeURIComponent(servicio.mensajeWhatsapp));
  });

  // ---- Rediseno F10e (ISS-134) --------------------------------------

  it('muestra la miga de pan con Inicio y el servicio actual', async () => {
    const servicio = SERVICIOS[0];
    const fixture = TestBed.createComponent(ServicioPage);
    fixture.componentRef.setInput('slug', servicio.slug);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    const miga = el.querySelector('nav[aria-label="Miga de pan"]');
    expect(miga).toBeTruthy();
    expect(miga?.querySelector('a[href="/"]')?.textContent).toContain('Inicio');
    expect(miga?.querySelector('[aria-current="page"]')?.textContent).toContain(servicio.nombre);
  });

  it('el aside invita al diagnostico digital con su enlace anclado', async () => {
    const servicio = SERVICIOS[0];
    const fixture = TestBed.createComponent(ServicioPage);
    fixture.componentRef.setInput('slug', servicio.slug);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    const aside = el.querySelector('.servicio-aside');
    expect(aside).toBeTruthy();
    expect(aside?.textContent).toContain(SERVICIO_ASIDE.titulo);
    expect(aside?.textContent).toContain(SERVICIO_ASIDE.texto);
    expect(aside?.querySelector('a[href="/herramientas#diagnostico"]')?.textContent).toContain(
      SERVICIO_ASIDE.cta,
    );
  });

  it('usa los titulos del prototipo: Lo que resolvemos y Como trabajamos', async () => {
    const servicio = SERVICIOS[0];
    const fixture = TestBed.createComponent(ServicioPage);
    fixture.componentRef.setInput('slug', servicio.slug);
    await fixture.whenStable();
    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(texto).toContain('Lo que resolvemos');
    expect(texto).toContain('Cómo trabajamos');
    expect(texto).not.toContain('El problema que resolvemos');
    expect(texto).not.toContain('Nuestro proceso');
  });

  it('establece el title, la meta description y og:title propios del servicio (no genericos)', async () => {
    const servicio = SERVICIOS[1];
    const fixture = TestBed.createComponent(ServicioPage);
    fixture.componentRef.setInput('slug', servicio.slug);
    await fixture.whenStable();

    expect(TestBed.inject(Title).getTitle()).toBe(`${servicio.nombre} — Crear Code Cesar`);
    expect(TestBed.inject(Meta).getTag('name="description"')?.content).toBe(servicio.resumenCorto);
    expect(TestBed.inject(Meta).getTag('property="og:url"')?.content).toContain(`/servicios/${servicio.slug}`);
  });
});
