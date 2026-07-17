import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CASOS } from '../../../contenido/casos';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

@Component({
  selector: 'app-pagina-caso-detalle',
  imports: [RouterLink],
  templateUrl: './caso-detalle.html',
  styleUrl: './caso-detalle.scss',
})
export class CasoDetallePage {
  readonly slug = input.required<string>();

  protected readonly caso = computed(() => CASOS.find((c) => c.slug === this.slug()));

  constructor() {
    establecerMetadatosDePagina(() => {
      const c = this.caso();
      return c
        ? { titulo: `${c.titulo} — Crear Code Cesar`, descripcion: c.metaDescripcion, ruta: `/casos/${c.slug}` }
        : undefined;
    });
  }
}
