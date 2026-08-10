import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { DEMO_DISENO } from '../../../contenido/demo-diseno';
import { DemoDiseno } from './demo-diseno';

const BOCETO = {
  titulo: 'App de pedidos para tu restaurante',
  funcionalidades: ['Menú digital', 'Pedidos a cocina', 'Pago con link', 'Seguimiento', 'Reporte diario'],
  imagenBase64: 'aW1hZ2VuLWZha2U=',
  tipoMime: 'image/png',
};

function iniciarSesionFake() {
  sessionStorage.setItem(
    'crearcode-sesion',
    JSON.stringify({ token: 'token-fake', rol: 'CLIENTE', correo: 'cliente@correo.com' }),
  );
}

async function crear() {
  const fixture = TestBed.createComponent(DemoDiseno);
  await fixture.whenStable();
  return { fixture, el: fixture.nativeElement as HTMLElement };
}

function escribir(el: HTMLElement, selector: string, valor: string) {
  const campo = el.querySelector<HTMLInputElement | HTMLTextAreaElement>(selector)!;
  campo.value = valor;
  campo.dispatchEvent(new Event('input'));
}

async function llenarYGenerar(fixture: Awaited<ReturnType<typeof crear>>['fixture']) {
  const el = fixture.nativeElement as HTMLElement;
  escribir(el, '#demo-sector', 'Restaurante');
  escribir(el, '#demo-que-hace', 'Vendemos almuerzos y domicilios');
  escribir(el, '#demo-que-necesita', 'Recibir pedidos sin saturar el WhatsApp');
  await fixture.whenStable();
  (el.querySelector('.demo-generar') as HTMLButtonElement).click();
  await fixture.whenStable();
}

describe('DemoDiseno (F10d, HU-42)', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('sin sesion muestra el estado bloqueado con CTAs a registro e ingreso', async () => {
    const { el } = await crear();

    expect(el.textContent).toContain(DEMO_DISENO.bloqueado.titulo);
    expect(el.querySelector('a[href="/registro"]')).toBeTruthy();
    expect(el.querySelector('a[href="/ingreso"]')).toBeTruthy();
    expect(el.querySelector('#demo-sector')).toBeNull();
  });

  it('con sesion muestra el formulario y el boton se habilita al llenar los 3 campos', async () => {
    iniciarSesionFake();
    const { fixture, el } = await crear();

    const boton = el.querySelector<HTMLButtonElement>('.demo-generar')!;
    expect(boton).toBeTruthy();
    expect(boton.disabled).toBe(true);

    escribir(el, '#demo-sector', 'Restaurante');
    escribir(el, '#demo-que-hace', 'Vendemos almuerzos');
    escribir(el, '#demo-que-necesita', 'Pedidos ordenados');
    await fixture.whenStable();

    expect(boton.disabled).toBe(false);
  });

  it('genera el boceto: peticion correcta, estado generando y resultado con imagen segura', async () => {
    iniciarSesionFake();
    const { fixture, el } = await crear();
    await llenarYGenerar(fixture);

    expect(el.textContent).toContain(DEMO_DISENO.generando);

    const solicitud = httpMock.expectOne('/api/asistente/demo-diseno');
    expect(solicitud.request.body).toEqual({
      sector: 'Restaurante',
      queHace: 'Vendemos almuerzos y domicilios',
      queNecesita: 'Recibir pedidos sin saturar el WhatsApp',
    });
    solicitud.flush(BOCETO);
    await fixture.whenStable();

    expect(el.textContent).toContain(BOCETO.titulo);
    expect(el.querySelectorAll('.demo-funcionalidad')).toHaveLength(5);
    const imagen = el.querySelector<HTMLImageElement>('.demo-imagen img')!;
    expect(imagen.getAttribute('src')).toBe('data:image/png;base64,aW1hZ2VuLWZha2U=');
    expect(imagen.getAttribute('alt')).toBe(DEMO_DISENO.resultado.altImagen);
    expect(el.querySelector('a[href="/contacto"]')).toBeTruthy();
  });

  it('la variacion se puede pedir UNA sola vez (HU-42)', async () => {
    iniciarSesionFake();
    const { fixture, el } = await crear();
    await llenarYGenerar(fixture);
    httpMock.expectOne('/api/asistente/demo-diseno').flush(BOCETO);
    await fixture.whenStable();

    const variacion = el.querySelector<HTMLButtonElement>('.demo-variacion')!;
    expect(variacion).toBeTruthy();
    variacion.click();
    await fixture.whenStable();

    httpMock.expectOne('/api/asistente/demo-diseno').flush(BOCETO);
    await fixture.whenStable();

    expect(el.querySelector('.demo-variacion')).toBeNull();
  });

  it('el limite diario muestra su mensaje y el error amable permite reintentar', async () => {
    iniciarSesionFake();
    const { fixture, el } = await crear();
    await llenarYGenerar(fixture);
    httpMock.expectOne('/api/asistente/demo-diseno').flush(
      { mensaje: 'límite', codigo: 'limite-registrado' },
      { status: 429, statusText: 'Too Many Requests' },
    );
    await fixture.whenStable();
    expect(el.textContent).toContain('Usaste tus bocetos de hoy');

    (el.querySelector('.demo-reiniciar') as HTMLButtonElement).click();
    await fixture.whenStable();
    await llenarYGenerar(fixture);
    httpMock.expectOne('/api/asistente/demo-diseno').flush('error', {
      status: 503,
      statusText: 'Service Unavailable',
    });
    await fixture.whenStable();

    expect(el.textContent).toContain('No perdiste ningún boceto');
    expect(el.querySelector('.demo-reintentar')).toBeTruthy();
  });
});
