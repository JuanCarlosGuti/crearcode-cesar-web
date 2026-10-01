import { Component, computed, input } from '@angular/core';

/**
 * Barra de avance del proyecto (F12). Es un progressbar de verdad para
 * los lectores de pantalla, con el porcentaje también en texto: el color
 * solo no le dice nada a quien no lo ve.
 */
@Component({
  selector: 'app-barra-de-avance',
  template: `
    <div
      class="barra"
      role="progressbar"
      [attr.aria-valuenow]="porcentaje()"
      aria-valuemin="0"
      aria-valuemax="100"
      [attr.aria-label]="etiqueta()"
    >
      <div class="barra__relleno" [style.width.%]="acotado()"></div>
    </div>
    <p class="barra__texto">
      <span class="barra__etiqueta">{{ etiqueta() }}</span>
      <strong>{{ porcentaje() }} %</strong>
    </p>
  `,
  styles: `
    :host {
      display: block;
    }

    .barra {
      height: 0.75rem;
      border-radius: 999px;
      background: var(--color-superficie-2);
      box-shadow: var(--sombra-1);
      overflow: hidden;
    }

    .barra__relleno {
      height: 100%;
      border-radius: inherit;
      background: var(--color-acento);
      transition: width 600ms ease-out;
    }

    .barra__texto {
      display: flex;
      justify-content: space-between;
      gap: var(--espacio-1);
      margin: var(--espacio-1) 0 0;
      font-size: 0.9rem;
    }

    .barra__etiqueta {
      color: var(--color-texto-secundario);
    }

    @media (prefers-reduced-motion: reduce) {
      .barra__relleno {
        transition: none;
      }
    }
  `,
})
export class BarraDeAvance {
  readonly porcentaje = input.required<number>();
  readonly etiqueta = input('Avance');

  protected readonly acotado = computed(() => Math.min(100, Math.max(0, this.porcentaje())));
}
