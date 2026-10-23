import { Component, computed, input } from '@angular/core';

import { DEMO_DISENO } from '../../../contenido/demo-diseno';
import { IdentidadDeMarca, PantallaDeBoceto } from '../../api/demo-api';
import { LogoDeMarca } from '../logo-de-marca/logo-de-marca';
import { letraDe, paletaDe, variablesDePaleta } from './paletas';

/**
 * Dibuja el boceto que arma la IA (ISS-234, ADR-16): cada pantalla en
 * un celular, cada pieza del catálogo con su forma y los textos del
 * negocio interpolados —nada de lo que trae se pinta como HTML—. Desde
 * la decisión 35, con los colores de la marca, su logo y su nombre arriba
 * y un dibujo por elemento.
 *
 * <p>
 * Es un dibujo, no una app: no lleva botones, campos ni enlaces que se
 * puedan usar, para que el teclado y los lectores de pantalla no se
 * tropiecen con controles muertos. Lo único enfocable es la tira de
 * pantallas, que se desliza de lado.
 */
@Component({
  selector: 'app-boceto-de-pantallas',
  templateUrl: './boceto-de-pantallas.html',
  styleUrl: './boceto-de-pantallas.scss',
  imports: [LogoDeMarca],
})
export class BocetoDePantallas {
  readonly pantallas = input.required<PantallaDeBoceto[]>();
  readonly identidad = input.required<IdentidadDeMarca>();

  protected readonly textos = DEMO_DISENO.boceto;
  protected readonly variables = computed(() => variablesDePaleta(paletaDe(this.identidad().paleta)));
  protected readonly letra = computed(() => letraDe(this.identidad().logo.letra).familia);

  protected inicial(texto: string): string {
    return texto.trim().charAt(0).toUpperCase();
  }
}
