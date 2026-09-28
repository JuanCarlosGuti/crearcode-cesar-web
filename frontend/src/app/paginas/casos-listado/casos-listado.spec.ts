import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { CASOS } from '../../../contenido/casos';
import { CasosListadoPage } from './casos-listado';

describe('CasosListadoPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('muestra un enlace por cada caso del portafolio', async () => {
    const fixture = TestBed.createComponent(CasosListadoPage);
    await fixture.whenStable();

    const enlaces = fixture.nativeElement.querySelectorAll('a[href^="/casos/"]');

    expect(enlaces.length).toBe(CASOS.length);
    expect(fixture.nativeElement.textContent).toContain(CASOS[0].titulo);
  });

  it('deja claro que son proyectos propios y no trabajos de clientes', async () => {
    // El encuadre es el contenido aqui: sin esta frase, el lector
    // asume que hubo un cliente detras de cada proyecto.
    const fixture = TestBed.createComponent(CasosListadoPage);
    await fixture.whenStable();

    const texto: string = fixture.nativeElement.textContent;
    expect(texto).toContain('Proyectos propios');
    expect(texto).toContain('Todavía no mostramos trabajos de clientes');
  });

  it('enlaza cada proyecto a su sitio o su codigo, en una pestana nueva', async () => {
    const fixture = TestBed.createComponent(CasosListadoPage);
    await fixture.whenStable();

    for (const proyecto of CASOS) {
      const externo = fixture.nativeElement.querySelector(`a[href="${proyecto.enlace.url}"]`);
      expect(externo).not.toBeNull();
      // Sale del sitio: que no se lleve por delante la navegacion.
      expect(externo.getAttribute('target')).toBe('_blank');
      expect(externo.getAttribute('rel')).toContain('noopener');
    }
  });
});
