import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Cotizacion } from '../../api/cotizaciones-api';
import { DetalleCotizacionPage } from './detalle-cotizacion';

function cotizacion(parcial: Partial<Cotizacion> = {}): Cotizacion {
  return {
    id: 'id-1',
    numero: null,
    estado: 'BORRADOR',
    origenSolicitudId: null,
    clienteNombre: 'Panaderia El Trigal',
    clienteCorreo: 'cliente@empresa.com',
    clienteTelefono: null,
    clienteIdentificacion: null,
    impuestoPorcentaje: 19,
    notas: null,
    creadaEn: '2026-08-11T10:00:00Z',
    validaHasta: '2026-08-26T10:00:00Z',
    enviadaEn: null,
    respondidaEn: null,
    items: [{ descripcion: 'Desarrollo', cantidad: 1, valorUnitario: 1000000, subtotal: 1000000 }],
    subtotal: 1000000,
    impuesto: 190000,
    total: 1190000,
    ...parcial,
  };
}

async function crear(estado: Cotizacion['estado'] = 'BORRADOR') {
  const fixture = TestBed.createComponent(DetalleCotizacionPage);
  fixture.componentRef.setInput('id', 'id-1');
  await fixture.whenStable();

  const httpMock = TestBed.inject(HttpTestingController);
  httpMock.expectOne('/api/cotizaciones/id-1').flush(
    cotizacion(estado === 'BORRADOR' ? {} : { estado, numero: 'COT-2026-0001' }),
  );
  await fixture.whenStable();

  return { fixture, el: fixture.nativeElement as HTMLElement, httpMock };
}

describe('DetalleCotizacionPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    globalThis.confirm = () => true;
  });

  it('un borrador se puede editar: muestra los campos de cada item', async () => {
    const { el } = await crear();

    expect(el.querySelector('#descripcion-0')).not.toBeNull();
    expect(el.querySelector('#cantidad-0')).not.toBeNull();
    expect(el.querySelector('#valor-0')).not.toBeNull();
  });

  it('agregar y quitar items cambia la lista editable', async () => {
    const { fixture, el } = await crear();
    const boton = (texto: string) =>
      Array.from(el.querySelectorAll('button')).find((b) =>
        b.textContent?.trim().startsWith(texto),
      ) as HTMLButtonElement;

    boton('Agregar').click();
    await fixture.whenStable();
    expect(el.querySelectorAll('.item-editable')).toHaveLength(2);

    boton('Quitar').click();
    await fixture.whenStable();
    expect(el.querySelectorAll('.item-editable')).toHaveLength(1);
  });

  it('el total previsto se recalcula al cambiar un item', async () => {
    const { fixture, el } = await crear();

    const cantidad = el.querySelector('#cantidad-0') as HTMLInputElement;
    cantidad.value = '3';
    cantidad.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    // 3 x 1.000.000 = 3.000.000 + 19% = 3.570.000
    expect(el.querySelector('.totales')?.textContent).toContain('3.570.000');
  });

  it('guardar envia los items al servidor', async () => {
    const { fixture, el, httpMock } = await crear();

    const guardar = Array.from(el.querySelectorAll('button')).find((b) =>
      b.textContent?.includes('Guardar'),
    ) as HTMLButtonElement;
    guardar.click();
    await fixture.whenStable();

    const peticion = httpMock.expectOne('/api/cotizaciones/id-1');
    expect(peticion.request.method).toBe('PUT');
    expect(peticion.request.body.items).toHaveLength(1);
  });

  // HU-45: enviar guarda primero, para que el cliente reciba lo que
  // esta en pantalla, y despues numera.
  it('enviar guarda y luego llama al envio', async () => {
    const { fixture, el, httpMock } = await crear();

    const enviar = Array.from(el.querySelectorAll('button')).find((b) =>
      b.textContent?.includes('Enviar'),
    ) as HTMLButtonElement;
    enviar.click();
    await fixture.whenStable();

    httpMock.expectOne((r) => r.url === '/api/cotizaciones/id-1' && r.method === 'PUT').flush(cotizacion());
    await fixture.whenStable();

    const envio = httpMock.expectOne('/api/cotizaciones/id-1/envio');
    expect(envio.request.method).toBe('POST');
  });

  // Invariante 1: lo que el cliente vio no se edita.
  it('una cotizacion enviada no muestra el formulario y avisa por que', async () => {
    const { el } = await crear('ENVIADA');

    expect(el.querySelector('#descripcion-0')).toBeNull();
    expect(el.textContent).toContain('ya fue enviada');
    expect(el.querySelector('table')).not.toBeNull();
  });

  it('un borrador sin items no se puede enviar', async () => {
    const { fixture, el } = await crear();

    const quitar = Array.from(el.querySelectorAll('button')).find((b) =>
      b.textContent?.includes('Quitar'),
    ) as HTMLButtonElement;
    quitar.click();
    await fixture.whenStable();

    const enviar = Array.from(el.querySelectorAll('button')).find((b) =>
      b.textContent?.includes('Enviar'),
    ) as HTMLButtonElement;
    expect(enviar.disabled).toBe(true);
    expect(el.textContent).toContain('Agrega al menos un ítem');
  });
});
