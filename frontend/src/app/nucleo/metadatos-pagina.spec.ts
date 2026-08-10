import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Meta, Title } from '@angular/platform-browser';

import { MetadatosDePagina, establecerMetadatosDePagina } from './metadatos-pagina';

@Component({ selector: 'app-anfitrion-de-prueba', template: '' })
class AnfitrionDePrueba {
  readonly datos = signal<MetadatosDePagina | undefined>(undefined);

  constructor() {
    establecerMetadatosDePagina(() => this.datos());
  }
}

describe('establecerMetadatosDePagina', () => {
  it('establece el title y la meta description', async () => {
    const fixture = TestBed.createComponent(AnfitrionDePrueba);
    fixture.componentInstance.datos.set({ titulo: 'Título de prueba', descripcion: 'Descripción de prueba', ruta: '/prueba' });
    await fixture.whenStable();

    expect(TestBed.inject(Title).getTitle()).toBe('Título de prueba');
    expect(TestBed.inject(Meta).getTag('name="description"')?.content).toBe('Descripción de prueba');
  });

  it('establece los tags Open Graph con la ruta absoluta y la imagen por defecto', async () => {
    const fixture = TestBed.createComponent(AnfitrionDePrueba);
    fixture.componentInstance.datos.set({ titulo: 'Título', descripcion: 'Descripción', ruta: '/servicios/x' });
    await fixture.whenStable();

    const meta = TestBed.inject(Meta);
    expect(meta.getTag('property="og:title"')?.content).toBe('Título');
    expect(meta.getTag('property="og:description"')?.content).toBe('Descripción');
    expect(meta.getTag('property="og:url"')?.content).toBe('https://crearcodecesar.com/servicios/x');
    expect(meta.getTag('property="og:image"')?.content).toBe('https://crearcodecesar.com/imagenes/og-defecto.jpg');
  });

  it('emite el link canonical con el dominio canonico (ADR-11)', async () => {
    const fixture = TestBed.createComponent(AnfitrionDePrueba);
    fixture.componentInstance.datos.set({ titulo: 'Título', descripcion: 'Descripción', ruta: '/herramientas' });
    await fixture.whenStable();

    const canonical = document.head.querySelector<HTMLLinkElement>('link[rel="canonical"]');
    expect(canonical?.getAttribute('href')).toBe('https://crearcodecesar.com/herramientas');

    // Navegar a otra pagina ACTUALIZA el canonical existente (no duplica)
    fixture.componentInstance.datos.set({ titulo: 'Otro', descripcion: 'Otra', ruta: '/casos' });
    await fixture.whenStable();
    expect(document.head.querySelectorAll('link[rel="canonical"]')).toHaveLength(1);
    expect(document.head.querySelector('link[rel="canonical"]')?.getAttribute('href')).toBe(
      'https://crearcodecesar.com/casos',
    );
  });

  it('usa la imagen propia de la pagina cuando se especifica', async () => {
    const fixture = TestBed.createComponent(AnfitrionDePrueba);
    fixture.componentInstance.datos.set({
      titulo: 'Título',
      descripcion: 'Descripción',
      ruta: '/blog/x',
      imagen: '/imagenes/blog-x.png',
    });
    await fixture.whenStable();

    expect(TestBed.inject(Meta).getTag('property="og:image"')?.content).toBe(
      'https://crearcodecesar.com/imagenes/blog-x.png',
    );
  });

  it('no falla si los datos aun no estan disponibles (ej. slug sin resolver)', async () => {
    expect(() => {
      const fixture = TestBed.createComponent(AnfitrionDePrueba);
      fixture.detectChanges();
    }).not.toThrow();
  });
});
