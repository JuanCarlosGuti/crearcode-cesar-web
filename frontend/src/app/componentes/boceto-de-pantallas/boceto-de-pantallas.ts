import { Component, input } from '@angular/core';

import { DEMO_DISENO } from '../../../contenido/demo-diseno';
import { PantallaDeBoceto } from '../../api/demo-api';

/**
 * Dibuja el boceto que arma la IA (ISS-234, ADR-16): cada pantalla en
 * un celular, cada pieza del catálogo con su forma y los textos del
 * negocio interpolados —nada de lo que trae se pinta como HTML—.
 *
 * <p>
 * Es un dibujo, no una app: no lleva botones, campos ni enlaces que se
 * puedan usar, para que el teclado y los lectores de pantalla no se
 * tropiecen con controles muertos. Lo único enfocable es la tira de
 * pantallas, que en el celular se desliza de lado.
 */
@Component({
  selector: 'app-boceto-de-pantallas',
  templateUrl: './boceto-de-pantallas.html',
  styleUrl: './boceto-de-pantallas.scss',
})
export class BocetoDePantallas {
  readonly pantallas = input.required<PantallaDeBoceto[]>();

  protected readonly textos = DEMO_DISENO.boceto;

  protected inicial(texto: string): string {
    return texto.trim().charAt(0).toUpperCase();
  }
}
