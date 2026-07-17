import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { SERVICIOS } from '../../../contenido/servicios';
import { TarjetaServicio } from './tarjeta-servicio';

describe('TarjetaServicio', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('enlaza a la ruta del servicio y muestra su nombre y resumen', async () => {
    const servicio = SERVICIOS[0];
    const fixture = TestBed.createComponent(TarjetaServicio);
    fixture.componentRef.setInput('servicio', servicio);
    await fixture.whenStable();

    const enlace = fixture.nativeElement.querySelector('a') as HTMLAnchorElement;

    expect(enlace.getAttribute('href')).toBe(`/servicios/${servicio.slug}`);
    expect(fixture.nativeElement.textContent).toContain(servicio.nombre);
    expect(fixture.nativeElement.textContent).toContain(servicio.resumenCorto);
  });
});
