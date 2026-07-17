import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Footer } from './footer';

describe('Footer', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([])],
    });
  });

  it('muestra los datos de contacto de la empresa', async () => {
    const fixture = TestBed.createComponent(Footer);
    await fixture.whenStable();

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Valledupar');
    expect(texto).toContain('323 988 5883');
    expect(texto).toContain('crearcodecesar@gmail.com');
  });

  it('incluye los enlaces legales', async () => {
    const fixture = TestBed.createComponent(Footer);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('a[href="/legales/politica-de-datos"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('a[href="/legales/terminos"]')).toBeTruthy();
  });

  it('muestra el anio actual en el copyright', async () => {
    const fixture = TestBed.createComponent(Footer);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain(String(new Date().getFullYear()));
  });
});
