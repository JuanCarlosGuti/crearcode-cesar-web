import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { SERVICIOS } from '../../../contenido/servicios';
import { AparecerAlVer } from '../../componentes/aparecer-al-ver/aparecer-al-ver';
import { Faq } from '../../componentes/faq/faq';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

@Component({
  selector: 'app-pagina-servicio',
  imports: [RouterLink, Faq, WhatsappCta, AparecerAlVer],
  templateUrl: './servicio.html',
  styleUrl: './servicio.scss',
})
export class ServicioPage {
  readonly slug = input.required<string>();

  protected readonly servicio = computed(() => SERVICIOS.find((s) => s.slug === this.slug()));

  constructor() {
    establecerMetadatosDePagina(() => {
      const s = this.servicio();
      return s
        ? { titulo: `${s.nombre} — Crear Code Cesar`, descripcion: s.resumenCorto, ruta: `/servicios/${s.slug}` }
        : undefined;
    });
  }
}
