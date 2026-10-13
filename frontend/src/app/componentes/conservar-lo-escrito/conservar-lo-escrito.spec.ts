import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FormField, form } from '@angular/forms/signals';

import { ConservarLoEscrito } from './conservar-lo-escrito';

interface Datos {
  nombre: string;
  mensaje: string;
  servicio: string;
  acepta: boolean;
}

const VACIO: Datos = { nombre: '', mensaje: '', servicio: '', acepta: false };

@Component({
  template: `
    <form appConservarLoEscrito>
      <input id="nombre" type="text" [formField]="formulario.nombre" />
      <textarea id="mensaje" [formField]="formulario.mensaje"></textarea>
      <select id="servicio" [formField]="formulario.servicio">
        <option value="">Elige uno</option>
        <option value="IA">IA</option>
      </select>
      <input id="acepta" type="checkbox" [formField]="formulario.acepta" />
    </form>
  `,
  imports: [FormField, ConservarLoEscrito],
})
class ConDirectiva {
  readonly datos = signal<Datos>({ ...VACIO });
  readonly formulario = form(this.datos);
}

// Los mismos campos, sin la directiva.
@Component({
  template: `
    <form>
      <input id="nombre" type="text" [formField]="formulario.nombre" />
      <textarea id="mensaje" [formField]="formulario.mensaje"></textarea>
      <select id="servicio" [formField]="formulario.servicio">
        <option value="">Elige uno</option>
        <option value="IA">IA</option>
      </select>
      <input id="acepta" type="checkbox" [formField]="formulario.acepta" />
    </form>
  `,
  imports: [FormField],
})
class SinDirectiva {
  readonly datos = signal<Datos>({ ...VACIO });
  readonly formulario = form(this.datos);
}

/**
 * Lo que pasa en un celular lento: la página prerenderizada ya está en
 * pantalla y el visitante escribe antes de que Angular hidrate. Aquí se
 * imita escribiendo en el DOM entre la creación de la vista y su
 * primera pasada de cambios, que es cuando `[formField]` escribe en el
 * campo el valor del modelo.
 */
function escribirAntesDeHidratar(raiz: HTMLElement) {
  raiz.querySelector<HTMLInputElement>('#nombre')!.value = 'Ana Pérez';
  raiz.querySelector<HTMLTextAreaElement>('#mensaje')!.value = 'Quiero una tienda en línea';
  raiz.querySelector<HTMLSelectElement>('#servicio')!.value = 'IA';
  raiz.querySelector<HTMLInputElement>('#acepta')!.checked = true;
}

describe('ConservarLoEscrito (ISS-228)', () => {
  it('sin la directiva, al hidratar se borra lo escrito: es el problema que resuelve', async () => {
    const fixture = TestBed.createComponent(SinDirectiva);
    const raiz = fixture.nativeElement as HTMLElement;
    escribirAntesDeHidratar(raiz);
    await fixture.whenStable();

    expect(raiz.querySelector<HTMLInputElement>('#nombre')!.value).toBe('');
    expect(fixture.componentInstance.datos()).toEqual(VACIO);
  });

  it('devuelve lo escrito al campo y al formulario: texto, área, lista y casilla', async () => {
    const fixture = TestBed.createComponent(ConDirectiva);
    const raiz = fixture.nativeElement as HTMLElement;
    escribirAntesDeHidratar(raiz);
    await fixture.whenStable();

    expect(raiz.querySelector<HTMLInputElement>('#nombre')!.value).toBe('Ana Pérez');
    expect(raiz.querySelector<HTMLTextAreaElement>('#mensaje')!.value).toBe('Quiero una tienda en línea');
    expect(raiz.querySelector<HTMLSelectElement>('#servicio')!.value).toBe('IA');
    expect(raiz.querySelector<HTMLInputElement>('#acepta')!.checked).toBe(true);
    expect(fixture.componentInstance.datos()).toEqual({
      nombre: 'Ana Pérez',
      mensaje: 'Quiero una tienda en línea',
      servicio: 'IA',
      acepta: true,
    });
  });

  it('no marca como tocados los campos que devuelve: los errores no aparecen antes de tiempo', async () => {
    const fixture = TestBed.createComponent(ConDirectiva);
    escribirAntesDeHidratar(fixture.nativeElement as HTMLElement);
    await fixture.whenStable();

    expect(fixture.componentInstance.formulario.nombre().touched()).toBe(false);
  });

  it('si no se escribió nada, no toca ningún campo', async () => {
    const fixture = TestBed.createComponent(ConDirectiva);
    const raiz = fixture.nativeElement as HTMLElement;
    const eventos = vi.fn();
    raiz.addEventListener('input', eventos);
    await fixture.whenStable();

    expect(eventos).not.toHaveBeenCalled();
    expect(fixture.componentInstance.datos()).toEqual(VACIO);
  });

  it('solo devuelve lo que cambió: lo demás queda como estaba', async () => {
    const fixture = TestBed.createComponent(ConDirectiva);
    const raiz = fixture.nativeElement as HTMLElement;
    raiz.querySelector<HTMLInputElement>('#nombre')!.value = 'Ana Pérez';
    const devueltos: string[] = [];
    raiz.addEventListener('input', (evento) => devueltos.push((evento.target as HTMLElement).id));
    await fixture.whenStable();

    expect(devueltos).toEqual(['nombre']);
  });
});
