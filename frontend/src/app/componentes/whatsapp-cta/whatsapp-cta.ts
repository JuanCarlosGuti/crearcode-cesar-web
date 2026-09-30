import { Component, computed, inject, input } from '@angular/core';

import { urlWhatsapp } from '../../../contenido/empresa';
import { Analitica } from '../../nucleo/analitica';

/**
 * Enlace a WhatsApp con el mensaje ya escrito.
 *
 * <p>
 * Es el punto por el que pasan los veinte CTAs de WhatsApp del sitio,
 * así que es también el único sitio donde hace falta instrumentar el
 * clic: con `origen` se distingue desde dónde salió (auditoría del 28
 * sep 2026, P1-1). Sin eso, «alguien escribió por WhatsApp» no dice si
 * lo hizo desde el pie o desde el cierre del diagnóstico, que es
 * justamente lo que decide dónde poner el esfuerzo.
 */
@Component({
  selector: 'app-whatsapp-cta',
  template: `
    <a
      class="boton boton-whatsapp"
      [href]="url()"
      target="_blank"
      rel="noopener"
      [attr.aria-label]="etiqueta()"
      (click)="registrarClic()"
    >
      {{ etiqueta() }}
    </a>
  `,
})
export class WhatsappCta {
  readonly mensaje = input.required<string>();
  readonly etiqueta = input('Escríbenos por WhatsApp');
  /** Desde dónde se pulsó: hero, footer, contacto, diagnostico… */
  readonly origen = input('sin-identificar');

  private readonly analitica = inject(Analitica);

  protected readonly url = computed(() => urlWhatsapp(this.mensaje()));

  protected registrarClic(): void {
    this.analitica.registrar('whatsapp_click', { origen: this.origen() });
  }
}
