import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { ResumenDeProyecto } from '../../api/proyectos-api';
import { ListadoProyectosPage } from './listado-proyectos';

function resumen(parcial: Partial<ResumenDeProyecto> = {}): ResumenDeProyecto {
  return {
    id: 'p-1',
    nombre: 'Tienda de Café Valle',
    clienteNombre: 'Café Valle',
    clienteCorreo: 'cliente@ejemplo.co',
    estado: 'ACTIVO',
    avance: 46,
    total: 14042000,
    saldo: 11614000,
    pendienteDePago: 4117000,
    actualizadoEn: '2026-10-01T15:00:00Z',
    ...parcial,
  };
}

async function crear(respuesta: object | null, estado = 200) {
  const fixture = TestBed.createComponent(ListadoProyectosPage);
  await fixture.whenStable();
  TestBed.inject(HttpTestingController)
    .expectOne('/api/proyectos')
    .flush(respuesta, { status: estado, statusText: estado === 200 ? 'OK' : 'Error' });
  await fixture.whenStable();
  return fixture.nativeElement as HTMLElement;
}

describe('ListadoProyectosPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('lista cada proyecto con su cliente, estado, avance y lo que hay por cobrar', async () => {
    const el = await crear([resumen()]);

    const fila = el.querySelector('tbody tr') as HTMLElement;
    expect(fila.querySelector('a[href="/admin/proyectos/p-1"]')?.textContent).toContain('Tienda de Café Valle');
    expect(fila.textContent).toContain('Café Valle');
    expect(fila.textContent).toContain('En marcha');
    expect(fila.textContent).toContain('46 %');
    expect(fila.textContent).toMatch(/4\.117\.000/);
  });

  it('ofrece crear un proyecto en blanco', async () => {
    const el = await crear([]);

    expect(el.querySelector('a[href="/admin/proyectos/nuevo"]')?.textContent).toContain('Proyecto en blanco');
    expect(el.textContent).toContain('Todavía no hay proyectos');
  });

  it('si falla la carga lo dice', async () => {
    const el = await crear(null, 500);

    expect(el.querySelector('[role="alert"]')?.textContent).toContain('No se pudo cargar');
  });
});
