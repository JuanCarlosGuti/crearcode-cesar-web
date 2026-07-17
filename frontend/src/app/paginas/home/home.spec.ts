import { TestBed } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';

import { HOME } from '../../../contenido/home';
import { METADATOS_HOME } from '../../../contenido/metadatos-paginas';
import { SERVICIOS } from '../../../contenido/servicios';
import { HomePage } from './home';

describe('HomePage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('muestra la propuesta de valor', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('h1')?.textContent).toBe(HOME.headline);
  });

  it('muestra una tarjeta por cada servicio', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();

    const tarjetas = fixture.nativeElement.querySelectorAll('app-tarjeta-servicio');

    expect(tarjetas.length).toBe(SERVICIOS.length);
  });

  it('incluye el CTA doble', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('a[href="/contacto"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('a[href^="https://wa.me/"]')).toBeTruthy();
  });

  it('establece el title de la Home', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();

    expect(TestBed.inject(Title).getTitle()).toBe(METADATOS_HOME.titulo);
  });
});
