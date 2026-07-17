import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { ARTICULOS } from '../../../contenido/blog';
import { BlogListadoPage } from './blog-listado';

describe('BlogListadoPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('muestra un enlace por cada articulo, del mas reciente al mas antiguo', async () => {
    const fixture = TestBed.createComponent(BlogListadoPage);
    await fixture.whenStable();

    const enlaces: HTMLAnchorElement[] = Array.from(
      fixture.nativeElement.querySelectorAll('a[href^="/blog/"]'),
    );

    expect(enlaces.length).toBe(ARTICULOS.length);
    const fechasOrdenadas = [...ARTICULOS].sort((a, b) => b.fecha.localeCompare(a.fecha));
    expect(enlaces[0].getAttribute('href')).toBe(`/blog/${fechasOrdenadas[0].slug}`);
  });
});
