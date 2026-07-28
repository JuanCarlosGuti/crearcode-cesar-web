import { isPlatformBrowser } from '@angular/common';
import { Directive, ElementRef, OnDestroy, OnInit, PLATFORM_ID, inject } from '@angular/core';

/**
 * Scroll-reveal (HU-35): el elemento aparece con una transición sutil
 * la primera vez que entra al viewport. Progressive enhancement: el
 * estado oculto lo aplica esta directiva desde JS, así que sin JS, en
 * SSR o con `prefers-reduced-motion` el contenido simplemente se ve —
 * nunca se oculta nada que no se pueda revelar. Sin dependencias de
 * Zone (IntersectionObserver + clases CSS: seguro en zoneless).
 */
@Directive({ selector: '[aparecerAlVer]' })
export class AparecerAlVer implements OnInit, OnDestroy {
  private readonly elemento = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly esNavegador = isPlatformBrowser(inject(PLATFORM_ID));
  private observador: IntersectionObserver | null = null;

  ngOnInit(): void {
    if (!this.esNavegador || typeof IntersectionObserver === 'undefined') {
      return;
    }
    const prefiereMenosMovimiento =
      typeof window.matchMedia === 'function' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (prefiereMenosMovimiento) {
      return;
    }

    const el = this.elemento.nativeElement;
    el.classList.add('aparecer-oculto');
    this.observador = new IntersectionObserver(
      (entradas) => {
        if (entradas.some((entrada) => entrada.isIntersecting)) {
          el.classList.add('aparecer-visible');
          el.classList.remove('aparecer-oculto');
          this.observador?.disconnect();
          this.observador = null;
        }
      },
      { threshold: 0.15 },
    );
    this.observador.observe(el);
  }

  ngOnDestroy(): void {
    this.observador?.disconnect();
  }
}
