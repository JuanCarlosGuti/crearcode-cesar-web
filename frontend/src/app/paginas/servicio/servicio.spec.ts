import { TestBed } from '@angular/core/testing';
import { Meta, Title } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';

import { SERVICIOS } from '../../../contenido/servicios';
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
