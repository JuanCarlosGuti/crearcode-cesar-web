import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { CASOS } from '../../../contenido/casos';
import { CasoDetallePage } from './caso-detalle';

describe('CasoDetallePage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('muestra reto, solucion y resultado del caso dado su slug', async () => {
    const caso = CASOS[0];
    const fixture = TestBed.createComponent(CasoDetallePage);
    fixture.componentRef.setInput('slug', caso.slug);
    await fixture.whenStable();

    const texto = fixture.nativeElement.textContent;
    expect(fixture.nativeElement.querySelector('h1')?.textContent).toBe(caso.titulo);
    expect(texto).toContain(caso.reto);
    expect(texto).toContain(caso.solucion);
    expect(texto).toContain(caso.resultado);
  });

  it('muestra un mensaje claro cuando el slug no corresponde a ningun caso', async () => {
    const fixture = TestBed.createComponent(CasoDetallePage);
    fixture.componentRef.setInput('slug', 'no-existe');
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Caso no encontrado');
  });
});
