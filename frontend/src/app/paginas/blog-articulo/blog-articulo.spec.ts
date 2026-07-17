import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { ARTICULOS } from '../../../contenido/blog';
import { BlogArticuloPage } from './blog-articulo';

describe('BlogArticuloPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('renderiza el titulo y el cuerpo en Markdown como HTML', async () => {
    const articulo = ARTICULOS[0];
    const fixture = TestBed.createComponent(BlogArticuloPage);
    fixture.componentRef.setInput('slug', articulo.slug);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('h1')?.textContent).toBe(articulo.titulo);
    const cuerpo = fixture.nativeElement.querySelector('.pagina-articulo__cuerpo');
    expect(cuerpo.querySelectorAll('h2').length).toBeGreaterThan(0);
    expect(cuerpo.textContent).toContain('WhatsApp');
  });

  it('muestra un mensaje claro cuando el slug no corresponde a ningun articulo', async () => {
    const fixture = TestBed.createComponent(BlogArticuloPage);
    fixture.componentRef.setInput('slug', 'no-existe');
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Artículo no encontrado');
  });
});
