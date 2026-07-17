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
});
