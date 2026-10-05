import { TestBed } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { Router, provideRouter } from '@angular/router';

import { ASISTENTE } from '../../../contenido/asistente';
import { TABLA_CUENTA } from '../../../contenido/cuenta';
import { HOME } from '../../../contenido/home';
import { METADATOS_HOME } from '../../../contenido/metadatos-paginas';
import { SERVICIOS } from '../../../contenido/servicios';
import { AsistenteUiService } from '../../nucleo/asistente-ui';
import { BocetoPendienteService, CLAVE_BOCETO_PENDIENTE } from '../../nucleo/boceto-pendiente';
import { HomePage } from './home';

function escribir(el: HTMLElement, selector: string, valor: string) {
  const campo = el.querySelector<HTMLInputElement>(selector)!;
  campo.value = valor;
  campo.dispatchEvent(new Event('input'));
}

describe('HomePage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  afterEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
    localStorage.removeItem(CLAVE_BOCETO_PENDIENTE);
  });

  it('muestra la propuesta de valor', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('h1')?.textContent).toBe(HOME.headline);
  });

  it('resalta el remate del titular sin alterar su texto', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const resaltado = fixture.nativeElement.querySelector('h1 .resaltado');
    expect(resaltado?.textContent).toBe('no al revés.');
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

  it('el hero muestra el gancho y la tarjeta del demo con sus tres campos editables (ISS-226)', async () => {
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.textContent).toContain(HOME.gancho);
    const demo = el.querySelector('.hero__demo')!;
    expect(demo.textContent).toContain(HOME.demo.titulo);
    for (const campo of HOME.demo.campos) {
      const entrada = demo.querySelector<HTMLInputElement>(`#hero-${campo.id}`)!;
      expect(entrada).not.toBeNull();
      expect(entrada.readOnly).toBe(false);
      expect(entrada.placeholder).toBe(campo.ejemplo);
      expect(entrada.maxLength).toBe(campo.maximo);
      expect(demo.querySelector(`label[for="hero-${campo.id}"]`)?.textContent).toContain(campo.etiqueta);
    }
    expect(demo.textContent).toContain(HOME.demo.notaPrivacidad);
  });

  it('sin sesion, pedir el boceto guarda lo escrito y lleva a crear la cuenta (ISS-226)', async () => {
    const router = TestBed.inject(Router);
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    escribir(el, '#hero-sector', 'Ferretería');
    escribir(el, '#hero-queHace', 'Vendemos materiales al por mayor');
    escribir(el, '#hero-queNecesita', 'Que los maestros pidan por WhatsApp');
    (el.querySelector('.hero__demo form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    expect(navegar).toHaveBeenCalledWith(['/registro']);
    expect(TestBed.inject(BocetoPendienteService).tomar()).toEqual({
      sector: 'Ferretería',
      queHace: 'Vendemos materiales al por mayor',
      queNecesita: 'Que los maestros pidan por WhatsApp',
    });
  });

  it('con sesion, pedir el boceto lleva directo al demo de herramientas (ISS-226)', async () => {
    sessionStorage.setItem(
      'crearcode-sesion',
      JSON.stringify({ token: 'token-fake', rol: 'CLIENTE', correo: 'cliente@correo.com' }),
    );
    const router = TestBed.inject(Router);
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    escribir(el, '#hero-sector', 'Clínica');
    (el.querySelector('.hero__demo form') as HTMLFormElement).requestSubmit();
    await fixture.whenStable();

    expect(navegar).toHaveBeenCalledWith(['/herramientas'], { fragment: 'demo-diseno' });
    expect(TestBed.inject(BocetoPendienteService).hay()).toBe(true);
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

  it('en el espacio que tuvieron los testimonios muestra proyectos reales y quien esta detras', async () => {
    // Paso por tres estados: testimonios ficticios (v1), placeholders
    // honestos (F10e) y, desde que hay proyectos publicos que enseñar,
    // los proyectos de verdad con su enlace.
    const fixture = TestBed.createComponent(HomePage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.textContent).toContain(HOME.proyectos.titulo);
    expect(el.textContent).toContain(HOME.fundador.titulo);
    expect(el.querySelector('a[href="/casos"]')).not.toBeNull();
    expect(el.querySelector('a[href="/sobre-nosotros"]')).not.toBeNull();
    expect(el.querySelector('.tarjeta-placeholder')).toBeNull();
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
