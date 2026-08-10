import { TestBed } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';

import { ASISTENTE } from '../../../contenido/asistente';
import { TABLA_CUENTA } from '../../../contenido/cuenta';
import { HOME } from '../../../contenido/home';
import { METADATOS_HOME } from '../../../contenido/metadatos-paginas';
import { SERVICIOS } from '../../../contenido/servicios';
import { AsistenteUiService } from '../../nucleo/asistente-ui';
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

  it('muestra la seccion de beneficios de la cuenta con sus tres tarjetas', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.textContent).toContain('Tu cuenta te da más');
    expect(el.querySelectorAll('.tarjeta-beneficio').length).toBe(3);
  });

  it('con F10 completa ningun beneficio lleva Muy pronto: todo esta vivo (ISS-131)', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelectorAll('.tarjeta-beneficio .badge')).toHaveLength(0);
  });

  it('la seccion de beneficios invita a crear cuenta o ingresar', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('a[href="/registro"]')).toBeTruthy();
    expect(el.querySelector('a[href="/ingreso"]')).toBeTruthy();
  });

  // ---- Rediseno F10e (ISS-133) --------------------------------------

  it('el hero muestra el gancho y la tarjeta del demo con enlace a herramientas', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.textContent).toContain(HOME.gancho);
    const demo = el.querySelector('.hero__demo');
    expect(demo).toBeTruthy();
    expect(demo?.textContent).toContain(HOME.demo.titulo);
    expect(demo?.querySelector('a[href="/herramientas#demo-diseno"]')).toBeTruthy();
  });

  it('la seccion de herramientas muestra las cuatro tarjetas y el CTA al centro', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    const tarjetas = el.querySelectorAll('.tarjeta-herramienta-home');
    expect(tarjetas.length).toBe(HOME.herramientas.tarjetas.length);
    for (const tarjeta of Array.from(tarjetas)) {
      expect(tarjeta.getAttribute('href')).toBe('/herramientas');
    }
    expect(el.textContent).toContain(HOME.herramientas.cta);
  });

  it('las preguntas sugeridas abren el asistente con la pregunta elegida', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const asistenteUi = TestBed.inject(AsistenteUiService);

    const botones = el.querySelectorAll<HTMLButtonElement>('.sugerencia-asistente');
    expect(botones.length).toBe(ASISTENTE.sugerencias.length);

    botones[0].click();

    expect(asistenteUi.aperturas()).toBe(1);
    expect(asistenteUi.consumirPregunta()).toBe(ASISTENTE.sugerencias[0]);
  });

  it('la tabla visitante vs cuenta muestra todas las filas y destaca la columna con cuenta', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    const filas = el.querySelectorAll('.tabla-cuenta tbody tr');
    expect(filas.length).toBe(TABLA_CUENTA.filas.length);
    expect(el.querySelector('.tabla-cuenta')?.textContent).toContain('Bocetos del demo de diseño');
    expect(el.querySelectorAll('.tabla-cuenta__destacado').length).toBe(TABLA_CUENTA.filas.length);
  });

  it('en vez de testimonios ficticios muestra los dos placeholders honestos', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    const placeholders = el.querySelectorAll('.tarjeta-placeholder');
    expect(placeholders.length).toBe(2);
    expect(el.textContent).toContain(HOME.placeholders.casos.titulo);
    expect(el.textContent).toContain(HOME.placeholders.equipo.titulo);
    expect(el.textContent).not.toContain('placeholder]');
  });

  it('cierra con el CTA de agenda y WhatsApp', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    const cierre = el.querySelector('.tarjeta-cierre');
    expect(cierre?.textContent).toContain(HOME.cierre.titulo);
    expect(cierre?.querySelector('a[href="/contacto"]')).toBeTruthy();
    expect(cierre?.querySelector('a[href^="https://wa.me/"]')).toBeTruthy();
  });
});
