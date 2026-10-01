import { Component, computed, input } from '@angular/core';

import { PROYECTOS } from '../../../contenido/proyectos';
import { EstadoEntregable } from '../../api/proyectos-api';

/**
 * Estado de un entregable con color, ícono **y** texto (F12): el color
 * solo no le dice nada a quien no distingue colores. Verde para lo
 * terminado, ámbar para lo que espera al cliente; nunca violeta, que en
 * este sitio es solo de la IA.
 */
@Component({
  selector: 'app-estado-de-entregable',
  template: `<span [class]="'estado-entregable estado-entregable--' + estado()"
    ><span class="estado-entregable__icono" aria-hidden="true">{{ icono() }}</span>{{ texto() }}</span
  >`,
  styles: `
    .estado-entregable {
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
      padding: 0.15rem 0.6rem;
      border-radius: 999px;
      font-size: 0.8rem;
      font-weight: 600;
      white-space: nowrap;
      border: 1px solid currentColor;
    }

    .estado-entregable__icono {
      font-family: var(--fuente-mono);
    }

    .estado-entregable--PENDIENTE {
      color: var(--color-texto-secundario);
    }

    .estado-entregable--EN_CURSO {
      color: var(--color-primario-claro);
    }

    .estado-entregable--EN_REVISION,
    .estado-entregable--CON_AJUSTES {
      color: var(--color-alerta);
    }

    .estado-entregable--APROBADO {
      color: var(--color-exito);
    }
  `,
})
export class EstadoDeEntregable {
  readonly estado = input.required<EstadoEntregable>();

  protected readonly texto = computed(() => PROYECTOS.estadosDelEntregable[this.estado()]);
  protected readonly icono = computed(() => ICONOS[this.estado()]);
}

const ICONOS: Record<EstadoEntregable, string> = {
  PENDIENTE: '○',
  EN_CURSO: '◐',
  EN_REVISION: '●',
  CON_AJUSTES: '↺',
  APROBADO: '✓',
};
