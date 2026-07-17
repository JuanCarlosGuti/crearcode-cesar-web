import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { Solicitud } from '../../api/solicitudes-api';
import { DetalleSolicitudPage } from './detalle-solicitud';

const SOLICITUD_NUEVA: Solicitud = {
  id: '11111111-1111-1111-1111-111111111111',
  nombre: 'Ana Pérez',
  empresa: 'Empresa S.A.S.',
  correo: 'ana@empresa.com',
  telefono: '3001234567',
  servicioDeInteres: 'OTRO',
  mensaje: 'Necesito ayuda con mi negocio.',
  estado: 'NUEVA',
  fechaCreacion: '2026-07-17T10:00:00Z',
  fechaUltimaActualizacion: '2026-07-17T10:00:00Z',
};

const SOLICITUD_TERMINAL: Solicitud = { ...SOLICITUD_NUEVA, id: '22222222-2222-2222-2222-222222222222', estado: 'CONVERTIDA' };

function crearFixture(id: string) {
  const fixture = TestBed.createComponent(DetalleSolicitudPage);
  fixture.componentRef.setInput('id', id);
  fixture.detectChanges();
  return fixture;
}

describe('DetalleSolicitudPage', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('muestra los datos completos de la solicitud dado su id', async () => {
    const fixture = crearFixture(SOLICITUD_NUEVA.id);
    httpMock.expectOne('/api/solicitudes').flush([SOLICITUD_NUEVA]);
    await fixture.whenStable();

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Ana Pérez');
    expect(texto).toContain('ana@empresa.com');
    expect(texto).toContain('3001234567');
    expect(texto).toContain('Necesito ayuda con mi negocio.');
  });

  it('en estado NUEVA ofrece las transiciones a CONTACTADA y DESCARTADA', async () => {
    const fixture = crearFixture(SOLICITUD_NUEVA.id);
    httpMock.expectOne('/api/solicitudes').flush([SOLICITUD_NUEVA]);
    await fixture.whenStable();

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Marcar como contactada');
    expect(texto).toContain('Descartar');
    expect(texto).not.toContain('Marcar como convertida');
  });

  it('en un estado terminal no ofrece ninguna transicion', async () => {
    const fixture = crearFixture(SOLICITUD_TERMINAL.id);
    httpMock.expectOne('/api/solicitudes').flush([SOLICITUD_TERMINAL]);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('.pagina-detalle__acciones')).toBeNull();
  });

  it('pide confirmacion antes de aplicar el cambio de estado', async () => {
    const fixture = crearFixture(SOLICITUD_NUEVA.id);
    httpMock.expectOne('/api/solicitudes').flush([SOLICITUD_NUEVA]);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    (Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.includes('Marcar como contactada')) as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(el.textContent).toContain('¿Confirmas que quieres marcar esta solicitud como contactada?');
    httpMock.expectNone('/api/solicitudes/11111111-1111-1111-1111-111111111111/estado');
  });

  it('al confirmar aplica el cambio de estado y lo refleja en pantalla', async () => {
    const fixture = crearFixture(SOLICITUD_NUEVA.id);
    httpMock.expectOne('/api/solicitudes').flush([SOLICITUD_NUEVA]);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    (Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.includes('Marcar como contactada')) as HTMLButtonElement).click();
    await fixture.whenStable();
    (Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.includes('Sí, confirmar')) as HTMLButtonElement).click();
    await fixture.whenStable();

    const peticion = httpMock.expectOne('/api/solicitudes/11111111-1111-1111-1111-111111111111/estado');
    expect(peticion.request.method).toBe('PATCH');
    expect(peticion.request.body).toEqual({ nuevoEstado: 'CONTACTADA' });
    peticion.flush(null);
    await fixture.whenStable();

    expect(el.textContent).toContain('CONTACTADA');
  });

  it('al cancelar la confirmacion no llama al backend', async () => {
    const fixture = crearFixture(SOLICITUD_NUEVA.id);
    httpMock.expectOne('/api/solicitudes').flush([SOLICITUD_NUEVA]);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    (Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.includes('Marcar como contactada')) as HTMLButtonElement).click();
    await fixture.whenStable();
    (Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.includes('Cancelar')) as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(el.textContent).not.toContain('¿Confirmas');
    httpMock.expectNone('/api/solicitudes/11111111-1111-1111-1111-111111111111/estado');
  });
});
