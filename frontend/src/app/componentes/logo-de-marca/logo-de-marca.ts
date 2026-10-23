import { Component, computed, input } from '@angular/core';

import { LogoDeMarca as Logo } from '../../api/demo-api';
import { letraDe, paletaDe } from '../boceto-de-pantallas/paletas';

/**
 * El logo que propone la IA (decisión 35), armado con piezas: una forma,
 * el símbolo —un emoji— o la inicial del nombre, y los colores y la letra
 * de la marca. Va siempre junto al nombre escrito, así que es decorativo
 * para los lectores de pantalla.
 */
@Component({
  selector: 'app-logo-de-marca',
  template: `<span
    class="logo"
    [class]="'logo logo--' + logo().forma.toLowerCase() + ' logo--' + tamano()"
    [style.background-color]="colores().acento"
    [style.color]="colores().sobreAcento"
    [style.font-family]="familia()"
    aria-hidden="true"
    >{{ contenido() }}</span
  >`,
  styleUrl: './logo-de-marca.scss',
})
export class LogoDeMarca {
  readonly nombre = input.required<string>();
  readonly logo = input.required<Logo>();
  readonly paleta = input.required<string>();
  readonly tamano = input<'grande' | 'pequeno'>('pequeno');

  protected readonly colores = computed(() => paletaDe(this.paleta()));
  protected readonly familia = computed(() => letraDe(this.logo().letra).familia);
  protected readonly contenido = computed(
    () => this.logo().simbolo || this.nombre().trim().charAt(0).toUpperCase(),
  );
}
