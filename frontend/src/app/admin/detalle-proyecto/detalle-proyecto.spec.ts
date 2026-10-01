import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Proyecto } from '../../api/proyectos-api';
import { entregableDePrueba, faseDePrueba, proyectoDePrueba } from '../../paginas/mi-proyecto/proyecto-de-prueba';
import { DetalleProyectoAdminPage } from './detalle-proyecto';

function boton(el: HTMLElement, texto: string): HTMLButtonElement {
  const encontrado = Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.trim() === texto);
  if (!encontrado) {
    throw new Error(`No hay un botón «${texto}»`);
  }
  return encontrado as HTMLButtonElement;
}

function escribir(el: HTMLElement, selector: string, valor: string) {
  const campo = el.querySelector(selector) as HTMLInputElement;
  campo.value = valor;
  campo.dispatchEvent(new Event('input'));
  campo.dispatchEvent(new Event('change'));
}

const BASE = '/api/proyectos/p-1';

async function crearCon(proyecto: Proyecto) {
  const fixture = TestBed.createComponent(DetalleProyectoAdminPage);
  fixture.componentRef.setInput('id', proyecto.id);
  await fixture.whenStable();
  const http = TestBed.inject(HttpTestingController);
  http.expectOne(BASE).flush(proyecto);
  await fixture.whenStable();
  return { fixture, el: fixture.nativeElement as HTMLElement, http };
}

describe('DetalleProyectoAdminPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('muestra el proyecto, el cliente y avisa si su correo todavía no tiene cuenta', async () => {
    const { el } = await crearCon(proyectoDePrueba({ clienteTieneCuenta: false }));

    expect(el.querySelector('h1')?.textContent).toContain('Tienda de Café Valle');
    expect(el.textContent).toContain('cliente@ejemplo.co');
    expect(el.textContent).toContain('todavía no tiene cuenta');
  });

  it('solo ofrece las transiciones válidas y las aplica', async () => {
    const { fixture, el, http } = await crearCon(proyectoDePrueba());

    expect(() => boton(el, 'Marcar aprobado')).toThrow();
    boton(el, 'Empezar').click();
    await fixture.whenStable();

    const peticion = http.expectOne(`${BASE}/entregables/e-diseno/estado`);
    expect(peticion.request.body).toEqual({ estado: 'EN_CURSO', nota: null });
    peticion.flush(
      proyectoDePrueba({ fases: [faseDePrueba({ entregables: [entregableDePrueba({ estado: 'EN_CURSO' })] })] }),
    );
    await fixture.whenStable();
    expect(boton(el, 'Enviar a revisión')).toBeTruthy();
  });

  /** Lo que vuelve de ajustes ya se había empezado: el botón no puede decir «Empezar». */
  it('lo que volvió con ajustes se retoma, no se empieza', async () => {
    const { el } = await crearCon(
      proyectoDePrueba({ fases: [faseDePrueba({ entregables: [entregableDePrueba({ estado: 'CON_AJUSTES' })] })] }),
    );

    expect(boton(el, 'Retomar')).toBeTruthy();
    expect(() => boton(el, 'Empezar')).toThrow();
  });

  it('devolver con ajustes exige la nota', async () => {
    const { fixture, el, http } = await crearCon(
      proyectoDePrueba({ fases: [faseDePrueba({ entregables: [entregableDePrueba({ estado: 'EN_REVISION' })] })] }),
    );

    boton(el, 'Devolver con ajustes').click();
    await fixture.whenStable();
    boton(el, 'Guardar').click();
    await fixture.whenStable();
    http.expectNone(`${BASE}/entregables/e-diseno/estado`);

    escribir(el, 'textarea[name="nota-ajustes"]', 'El logo más grande');
    boton(el, 'Guardar').click();
    await fixture.whenStable();
    expect(http.expectOne(`${BASE}/entregables/e-diseno/estado`).request.body).toEqual({
      estado: 'CON_AJUSTES',
      nota: 'El logo más grande',
    });
  });

  it('registra un pago con la entrega, el monto, la fecha y el medio', async () => {
    const { fixture, el, http } = await crearCon(proyectoDePrueba());

    escribir(el, '#pago-monto', '590000');
    escribir(el, '#pago-fecha', '2026-09-02');
    escribir(el, '#pago-medio', 'NEQUI_DAVIPLATA');
    escribir(el, '#pago-referencia', 'Comprobante 123');
    boton(el, 'Registrar pago').click();
    await fixture.whenStable();

    expect(http.expectOne(`${BASE}/pagos`).request.body).toEqual({
      entregableId: 'e-diseno',
      monto: 590000,
      fecha: '2026-09-02',
      medio: 'NEQUI_DAVIPLATA',
      referencia: 'Comprobante 123',
    });
  });

  /** Preseleccionar algo ya pagado lleva a registrar el pago donde no era. */
  it('el pago viene preseleccionado en la primera entrega que tiene algo por cobrar', async () => {
    const { fixture, el, http } = await crearCon(
      proyectoDePrueba({
        fases: [
          faseDePrueba({
            entregables: [
              entregableDePrueba({ id: 'e-pagado', nombre: 'Pagado', pagado: 1190000, pendiente: 0 }),
              entregableDePrueba({ id: 'e-debe', nombre: 'Debe', estado: 'APROBADO', pagado: 0, pendiente: 1190000 }),
            ],
          }),
        ],
      }),
    );

    escribir(el, '#pago-monto', '1000');
    boton(el, 'Registrar pago').click();
    await fixture.whenStable();

    expect(http.expectOne(`${BASE}/pagos`).request.body.entregableId).toBe('e-debe');
  });

  it('un pago que el servidor rechaza muestra su mensaje', async () => {
    const { fixture, el, http } = await crearCon(proyectoDePrueba());
    escribir(el, '#pago-monto', '9999999');
    boton(el, 'Registrar pago').click();
    await fixture.whenStable();

    http
      .expectOne(`${BASE}/pagos`)
      .flush({ mensaje: 'El pago supera lo que falta por pagar de ese entregable: 1190000' }, { status: 400, statusText: 'Bad Request' });
    await fixture.whenStable();

    expect(el.querySelector('[role="alert"]')?.textContent).toContain('supera lo que falta por pagar');
  });

  it('en un proyecto de cotización, lo nuevo entra siempre como cambio de alcance', async () => {
    const { fixture, el, http } = await crearCon(proyectoDePrueba({ origenCotizacionId: 'c-1' }));

    const casilla = el.querySelector('input[name="nuevo-cambio-de-alcance"]') as HTMLInputElement;
    expect(casilla.checked).toBe(true);
    expect(casilla.disabled).toBe(true);

    escribir(el, 'input[name="nuevo-entregable-nombre"]', 'Cupones');
    escribir(el, 'input[name="nuevo-entregable-valor"]', '800000');
    boton(el, 'Agregar entrega').click();
    await fixture.whenStable();

    const cuerpo = http.expectOne(`${BASE}/fases/f-1/entregables`).request.body;
    expect(cuerpo.esCambioDeAlcance).toBe(true);
    expect(cuerpo.entregable).toEqual({ nombre: 'Cupones', descripcion: null, valor: 800000, momentoDeCobro: 'AL_APROBAR' });
  });

  it('pausar pide confirmación en la página', async () => {
    const { fixture, el, http } = await crearCon(proyectoDePrueba());

    boton(el, 'Pausar proyecto').click();
    await fixture.whenStable();
    http.expectNone(`${BASE}/pausa`);
    expect(el.textContent).toContain('¿Pausar el proyecto?');

    boton(el, 'Sí, continuar').click();
    await fixture.whenStable();
    http.expectOne(`${BASE}/pausa`).flush(proyectoDePrueba({ estado: 'PAUSADO' }));
    await fixture.whenStable();
    expect(boton(el, 'Reanudar proyecto')).toBeTruthy();
  });

  it('reordena las fases con botones, sin depender de arrastrar', async () => {
    const { fixture, el, http } = await crearCon(
      proyectoDePrueba({ fases: [faseDePrueba(), faseDePrueba({ id: 'f-2', nombre: 'Construcción', entregables: [] })] }),
    );

    const subir = el.querySelector('button[aria-label="Subir fase Construcción"]') as HTMLButtonElement;
    subir.click();
    await fixture.whenStable();

    expect(http.expectOne(`${BASE}/fases/f-2/posicion`).request.body).toEqual({ posicion: 0 });
  });
});
