import { TestBed } from '@angular/core/testing';

import { POLITICA_DE_DATOS, TERMINOS_DE_USO } from '../../../contenido/legales';
import { LegalPage } from './legal';

describe('LegalPage', () => {
  it('muestra el titulo y todos los parrafos del documento recibido', async () => {
    const fixture = TestBed.createComponent(LegalPage);
    fixture.componentRef.setInput('documento', POLITICA_DE_DATOS);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('h1')?.textContent).toBe(POLITICA_DE_DATOS.titulo);
    const texto = fixture.nativeElement.textContent;
    POLITICA_DE_DATOS.parrafos.forEach((parrafo) => expect(texto).toContain(parrafo));
  });

  it('funciona igual para el documento de terminos de uso', async () => {
    const fixture = TestBed.createComponent(LegalPage);
    fixture.componentRef.setInput('documento', TERMINOS_DE_USO);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('h1')?.textContent).toBe(TERMINOS_DE_USO.titulo);
  });
});
