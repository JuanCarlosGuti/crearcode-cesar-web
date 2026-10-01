import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';

import { proyectoDePrueba } from '../../paginas/mi-proyecto/proyecto-de-prueba';
import { NuevoProyectoPage } from './nuevo-proyecto';

const COTIZACION = {
  id: 'c-1',
  numero: 'COT-2026-0001',
  estado: 'ACEPTADA',
  clienteNombre: 'Café Valle',
  clienteCorreo: 'cliente@ejemplo.co',
  impuestoPorcentaje: 19,
  subtotal: 5000000,
  impuesto: 950000,
  total: 5950000,
  items: [],
};

const PROPUESTA = [
  {
    fase: { nombre: 'Fase 1', objetivo: null, inicioPlaneado: null, finPlaneado: null },
    entregables: [
      { nombre: 'Módulo de diseño', descripcion: null, valor: 2000000, momentoDeCobro: 'AL_INICIAR' },
      { nombre: 'Catálogo', descripcion: null, valor: 3000000, momentoDeCobro: 'AL_APROBAR' },
    ],
  },
];

function escribir(el: HTMLElement, selector: string, valor: string) {
  const campo = el.querySelector(selector) as HTMLInputElement;
  campo.value = valor;
  campo.dispatchEvent(new Event('input'));
}

function boton(el: HTMLElement, texto: string): HTMLButtonElement {
  return Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.trim() === texto) as HTMLButtonElement;
}

async function crear(cotizacion?: string) {
  const fixture = TestBed.createComponent(NuevoProyectoPage);
  if (cotizacion) {
    fixture.componentRef.setInput('cotizacion', cotizacion);
  }
  await fixture.whenStable();
  return { fixture, el: fixture.nativeElement as HTMLElement, http: TestBed.inject(HttpTestingController) };
}

async function crearDesdeCotizacion() {
  const pagina = await crear('c-1');
  pagina.http.expectOne('/api/cotizaciones/c-1').flush(COTIZACION);
  pagina.http.expectOne('/api/proyectos/propuesta?cotizacion=c-1').flush(PROPUESTA);
  await pagina.fixture.whenStable();
  return pagina;
}

describe('NuevoProyectoPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('desde una cotización propone su plan y muestra que suma lo aceptado', async () => {
    const { el } = await crearDesdeCotizacion();

    expect(el.textContent).toContain('Desde la cotización COT-2026-0001, aceptada por Café Valle.');
    const nombres = Array.from(el.querySelectorAll<HTMLInputElement>('input[name="entregable-nombre"]')).map((i) => i.value);
    expect(nombres).toEqual(['Módulo de diseño', 'Catálogo']);
    expect(el.querySelector('.suma')?.textContent).toMatch(/5\.000\.000/);
    expect(el.querySelector('.suma__diferencia')).toBeNull();
    expect(boton(el, 'Crear proyecto').disabled).toBe(false);
  });

  /** Invariante 5: lo aceptado no se cambia en silencio; el botón no deja. */
  it('si el plan no suma lo aceptado muestra la diferencia y no deja crear', async () => {
    const { fixture, el } = await crearDesdeCotizacion();

    escribir(el, 'input[name="entregable-valor"]', '1000000');
    await fixture.whenStable();

    expect(el.querySelector('.suma__diferencia')?.textContent).toMatch(/1\.000\.000/);
    expect(boton(el, 'Crear proyecto').disabled).toBe(true);
  });

  it('crea desde la cotización y lleva al proyecto', async () => {
    const { fixture, el, http } = await crearDesdeCotizacion();
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    escribir(el, '#nombre-proyecto', 'Tienda de Café Valle');
    await fixture.whenStable();

    boton(el, 'Crear proyecto').click();
    await fixture.whenStable();

    const peticion = http.expectOne('/api/proyectos');
    expect(peticion.request.body.cotizacionId).toBe('c-1');
    expect(peticion.request.body.cliente).toBeNull();
    expect(peticion.request.body.descripcion.nombre).toBe('Tienda de Café Valle');
    expect(peticion.request.body.plan[0].entregables).toHaveLength(2);
    peticion.flush(proyectoDePrueba({ id: 'p-nuevo' }));
    await fixture.whenStable();

    expect(navegar).toHaveBeenCalledWith(['/admin/proyectos', 'p-nuevo']);
  });

  it('en blanco pide los datos del cliente y usa el IVA por defecto', async () => {
    const { fixture, el, http } = await crear();
    escribir(el, '#cliente-nombre', 'Panadería El Trigal');
    escribir(el, '#cliente-correo', 'trigal@ejemplo.co');
    escribir(el, '#nombre-proyecto', 'App de pedidos');
    await fixture.whenStable();

    boton(el, 'Crear proyecto').click();
    await fixture.whenStable();

    const cuerpo = http.expectOne('/api/proyectos').request.body;
    expect(cuerpo.cotizacionId).toBeNull();
    expect(cuerpo.cliente).toEqual({ nombre: 'Panadería El Trigal', correo: 'trigal@ejemplo.co' });
    expect(cuerpo.impuestoPorcentaje).toBe(19);
  });

  it('si el servidor rechaza, muestra su mensaje y no pierde lo escrito', async () => {
    const { fixture, el, http } = await crearDesdeCotizacion();
    escribir(el, '#nombre-proyecto', 'Tienda');
    await fixture.whenStable();
    boton(el, 'Crear proyecto').click();
    await fixture.whenStable();

    http.expectOne('/api/proyectos').flush(
      { mensaje: 'La cotización c-1 ya tiene un proyecto' },
      { status: 409, statusText: 'Conflict' },
    );
    await fixture.whenStable();

    expect(el.querySelector('[role="alert"]')?.textContent).toContain('ya tiene un proyecto');
    expect((el.querySelector('#nombre-proyecto') as HTMLInputElement).value).toBe('Tienda');
  });
});
