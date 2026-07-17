import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { SERVICIOS } from '../../../contenido/servicios';
import { Faq } from '../../componentes/faq/faq';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';

@Component({
  selector: 'app-pagina-servicio',
  imports: [RouterLink, Faq, WhatsappCta],
  templateUrl: './servicio.html',
  styleUrl: './servicio.scss',
})
export class ServicioPage {
  readonly slug = input.required<string>();

  protected readonly servicio = computed(() => SERVICIOS.find((s) => s.slug === this.slug()));
}
