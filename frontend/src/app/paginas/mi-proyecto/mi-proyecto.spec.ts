import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Proyecto } from '../../api/proyectos-api';
import { MiProyectoPage } from './mi-proyecto';
import { entregableDePrueba, faseDePrueba, proyectoDePrueba } from './proyecto-de-prueba';

async function crear(id = 'p-1') {
  const fixture = TestBed.createComponent(MiProyectoPage);
  fixture.componentRef.setInput('id', id);
  await fixture.whenStable();
  return { fixture, el: fixture.nativeElement as HTMLElement, http: TestBed.inject(HttpTestingController) };
}

async function crearCon(proyecto: Proyecto) {
  const pagina = await crear(proyecto.id);
  pagina.http.expectOne(`/api/mis-proyectos/${proyecto.id}`).flush(proyecto);
  await pagina.fixture.whenStable();
  return pagina;
}

function botonQueDice(el: HTMLElement, texto: string): HTMLButtonElement {
  const boton = Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.trim() === texto);
  if (!boton) {
    throw new Error(`No hay un botón que diga «${texto}»`);
  }
  return boton as HTMLButtonElement;
}

const EN_REVISION = proyectoDePrueba({
  fases: [
    faseDePrueba({
      entregables: [
        entregableDePrueba({ estado: 'EN_REVISION', urlDemo: 'https://figma.com/proto/cafe' }),
        entregableDePrueba({ id: 'e-catalogo', nombre: 'Catálogo', estado: 'PENDIENTE', esCobrable: false }),
      ],
    }),
  ],
});

describe('MiProyectoPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('muestra el encabezado con el avance como barra accesible y las fechas sin correrse de día', async () => {
    const { el } = await crearCon(proyectoDePrueba());

    expect(el.querySelector('h1')?.textContent).toContain('Tienda de Café Valle');
    const barra = el.querySelector('[role="progressbar"]');
    expect(barra?.getAttribute('aria-valuenow')).toBe('25');
    expect(el.textContent).toContain('1 de septiembre de 2026');
    expect(el.textContent).toContain('15 de diciembre de 2026');
    expect(el.textContent).toContain('En marcha');
  });

  it('en garantía dice hasta cuándo', async () => {
    const { el } = await crearCon(proyectoDePrueba({ estado: 'EN_GARANTIA', finDeGarantia: '2026-11-30T15:00:00Z' }));

    expect(el.textContent).toContain('Garantía hasta el 30 de noviembre de 2026');
  });

  it('muestra cada fase con sus entregables y el estado en texto, no solo en color', async () => {
    const { el } = await crearCon(EN_REVISION);

    expect(el.textContent).toContain('Diseño de la tienda');
    expect(el.textContent).toContain('Que veas cómo se verá');
    const estados = Array.from(el.querySelectorAll('.estado-entregable')).map((e) => e.textContent?.trim());
    expect(estados).toEqual(
      expect.arrayContaining([expect.stringContaining('Para revisar'), expect.stringContaining('Pendiente')]),
    );
  });

  it('destaca lo que está listo para revisar', async () => {
    const { el } = await crearCon(EN_REVISION);

    const destacado = el.querySelector('.para-revisar');
    expect(destacado?.textContent).toContain('Listo para que lo mires');
    expect(destacado?.textContent).toContain('Diseño');
  });

  it('el enlace de la demo abre en otra pestaña sin darle acceso a esta', async () => {
    const { el } = await crearCon(EN_REVISION);

    const demo = el.querySelector('a.ver-demo') as HTMLAnchorElement;
    expect(demo.href).toBe('https://figma.com/proto/cafe');
    expect(demo.target).toBe('_blank');
    expect(demo.rel).toContain('noopener');
    expect(demo.rel).toContain('noreferrer');
  });

  it('aprobar pide confirmación en la página y luego muestra el proyecto actualizado', async () => {
    const { fixture, el, http } = await crearCon(EN_REVISION);

    botonQueDice(el, 'Aprobar').click();
    await fixture.whenStable();
    expect(el.textContent).toContain('¿Apruebas «Diseño»?');
    http.expectNone('/api/mis-proyectos/p-1/entregables/e-diseno/aprobacion');

    botonQueDice(el, 'Sí, aprobar').click();
    await fixture.whenStable();
    http.expectOne('/api/mis-proyectos/p-1/entregables/e-diseno/aprobacion').flush(
      proyectoDePrueba({
        avance: 50,
        fases: [faseDePrueba({ entregables: [entregableDePrueba({ estado: 'APROBADO', aprobadoPor: 'CLIENTE', aprobadoEn: '2026-09-20T15:00:00Z' })] })],
      }),
    );
    await fixture.whenStable();

    expect(el.querySelector('[role="progressbar"]')?.getAttribute('aria-valuenow')).toBe('50');
    expect(el.textContent).toContain('Lo aprobaste el 20 de septiembre de 2026');
    expect(el.querySelector('.para-revisar')).toBeNull();
  });

  it('pedir ajustes exige escribir qué ajustar antes de enviar', async () => {
    const { fixture, el, http } = await crearCon(EN_REVISION);

    botonQueDice(el, 'Pedir ajustes').click();
    await fixture.whenStable();
    botonQueDice(el, 'Enviar ajustes').click();
    await fixture.whenStable();
    expect(el.textContent).toContain('Escribe qué hay que ajustar.');
    http.expectNone('/api/mis-proyectos/p-1/entregables/e-diseno/ajustes');

    const nota = el.querySelector('textarea') as HTMLTextAreaElement;
    nota.value = 'El logo más grande';
    nota.dispatchEvent(new Event('input'));
    botonQueDice(el, 'Enviar ajustes').click();
    await fixture.whenStable();

    const peticion = http.expectOne('/api/mis-proyectos/p-1/entregables/e-diseno/ajustes');
    expect(peticion.request.body).toEqual({ nota: 'El logo más grande' });
    peticion.flush(
      proyectoDePrueba({
        fases: [faseDePrueba({ entregables: [entregableDePrueba({ estado: 'CON_AJUSTES', notaDeAjustes: 'El logo más grande' })] })],
      }),
    );
    await fixture.whenStable();
    expect(el.textContent).toContain('Qué se está ajustando:');
    expect(el.textContent).toContain('El logo más grande');
  });

  it('si la respuesta falla lo dice y no pierde lo escrito', async () => {
    const { fixture, el, http } = await crearCon(EN_REVISION);
    botonQueDice(el, 'Pedir ajustes').click();
    await fixture.whenStable();
    const nota = el.querySelector('textarea') as HTMLTextAreaElement;
    nota.value = 'Cambiar colores';
    nota.dispatchEvent(new Event('input'));
    botonQueDice(el, 'Enviar ajustes').click();
    await fixture.whenStable();

    http.expectOne('/api/mis-proyectos/p-1/entregables/e-diseno/ajustes').flush(null, { status: 500, statusText: 'Error' });
    await fixture.whenStable();

    expect(el.textContent).toContain('No pudimos guardar tu respuesta.');
    expect((el.querySelector('textarea') as HTMLTextAreaElement).value).toBe('Cambiar colores');
  });

  it('los pagos muestran lo que se cobra, lo pagado, lo pendiente y que no es una factura', async () => {
    const { el } = await crearCon(
      proyectoDePrueba({
        totalPagado: 590000,
        saldo: 4170000,
        pendienteDePago: 600000,
        fases: [faseDePrueba({ entregables: [entregableDePrueba({ pagado: 590000, pendiente: 600000 })] })],
        pagos: [
          { id: 'pago-1', entregableId: 'e-diseno', monto: 590000, fecha: '2026-09-02', medio: 'NEQUI_DAVIPLATA', origen: 'MANUAL', referencia: 'Comprobante 123' },
        ],
      }),
    );

    const pagos = el.querySelector('.pagos') as HTMLElement;
    expect(pagos.textContent).toMatch(/4\.760\.000/);
    expect(pagos.textContent).toMatch(/590\.000/);
    expect(pagos.textContent).toMatch(/4\.170\.000/);
    expect(pagos.textContent).toMatch(/600\.000/);
    expect(pagos.textContent).toContain('Nequi / Daviplata');
    expect(pagos.textContent).toContain('2 de septiembre de 2026');
    expect(pagos.textContent).toContain('no es una factura');
  });

  it('lo que todavía no se cobra no aparece como deuda', async () => {
    const { el } = await crearCon(EN_REVISION);

    expect(el.querySelector('.pagos')?.textContent).toContain('Se cobra al aprobarse');
  });

  it('sin fases dice que el plan se está armando', async () => {
    const { el } = await crearCon(proyectoDePrueba({ fases: [], avance: 0 }));

    expect(el.textContent).toContain('Estamos armando tu plan');
  });

  it('si falla la carga ofrece reintentar', async () => {
    const { fixture, el, http } = await crear();
    http.expectOne('/api/mis-proyectos/p-1').flush(null, { status: 500, statusText: 'Error' });
    await fixture.whenStable();
    expect(el.textContent).toContain('No pudimos cargar tu proyecto');

    botonQueDice(el, 'Reintentar').click();
    await fixture.whenStable();
    http.expectOne('/api/mis-proyectos/p-1').flush(proyectoDePrueba());
    await fixture.whenStable();

    expect(el.querySelector('h1')?.textContent).toContain('Tienda de Café Valle');
  });

  it('un proyecto ajeno o inexistente dice que no está en la cuenta, sin reintentar', async () => {
    const { fixture, el, http } = await crear('ajeno');
    http.expectOne('/api/mis-proyectos/ajeno').flush(null, { status: 404, statusText: 'Not Found' });
    await fixture.whenStable();

    expect(el.textContent).toContain('No encontramos este proyecto en tu cuenta.');
    expect(Array.from(el.querySelectorAll('button')).some((b) => b.textContent?.includes('Reintentar'))).toBe(false);
  });
});
