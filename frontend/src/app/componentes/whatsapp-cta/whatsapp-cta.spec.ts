import { TestBed } from '@angular/core/testing';

import { WhatsappCta } from './whatsapp-cta';

describe('WhatsappCta', () => {
  it('arma la URL de wa.me con el numero y el mensaje codificado', async () => {
    const fixture = TestBed.createComponent(WhatsappCta);
    fixture.componentRef.setInput('mensaje', 'Hola, quiero información');
    await fixture.whenStable();

    const enlace = fixture.nativeElement.querySelector('a') as HTMLAnchorElement;

    expect(enlace.href).toContain('https://wa.me/573239885883');
    expect(enlace.href).toContain(encodeURIComponent('Hola, quiero información'));
  });

  it('usa la etiqueta por defecto cuando no se especifica una', async () => {
    const fixture = TestBed.createComponent(WhatsappCta);
    fixture.componentRef.setInput('mensaje', 'mensaje');
    await fixture.whenStable();

    const enlace = fixture.nativeElement.querySelector('a') as HTMLAnchorElement;

    expect(enlace.textContent?.trim()).toBe('Escríbenos por WhatsApp');
  });

  it('usa una etiqueta personalizada cuando se especifica', async () => {
    const fixture = TestBed.createComponent(WhatsappCta);
    fixture.componentRef.setInput('mensaje', 'mensaje');
    fixture.componentRef.setInput('etiqueta', 'Escríbenos ahora');
    await fixture.whenStable();

    const enlace = fixture.nativeElement.querySelector('a') as HTMLAnchorElement;

    expect(enlace.textContent?.trim()).toBe('Escríbenos ahora');
  });
});
