import { Component, computed, input } from '@angular/core';

import { DEMO_DISENO } from '../../../contenido/demo-diseno';
import { IdentidadDeMarca as Identidad } from '../../api/demo-api';
import { letraDe, paletaDe } from '../boceto-de-pantallas/paletas';
import { LogoDeMarca } from '../logo-de-marca/logo-de-marca';

/**
 * La tarjeta «Tu identidad de marca» del demo (decisión 35): el logo, el
 * nombre con su letra, el eslogan, los colores y la letra elegidos. Dice
 * que es una propuesta de referencia y recuerda verificar el nombre: no
 * prometemos un logo terminado ni un nombre que alguien más ya usa.
 */
@Component({
  selector: 'app-identidad-de-marca',
  templateUrl: './identidad-de-marca.html',
  styleUrl: './identidad-de-marca.scss',
  imports: [LogoDeMarca],
})
export class IdentidadDeMarca {
  readonly identidad = input.required<Identidad>();

  protected readonly textos = DEMO_DISENO.identidad;
  protected readonly paleta = computed(() => paletaDe(this.identidad().paleta));
  protected readonly letra = computed(() => letraDe(this.identidad().logo.letra));
}
