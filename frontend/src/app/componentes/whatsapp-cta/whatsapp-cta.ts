import { Component, computed, input } from '@angular/core';

import { urlWhatsapp } from '../../../contenido/empresa';

@Component({
  selector: 'app-whatsapp-cta',
  template: `
    <a class="boton boton-whatsapp" [href]="url()" target="_blank" rel="noopener" [attr.aria-label]="etiqueta()">
      {{ etiqueta() }}
    </a>
  `,
})
export class WhatsappCta {
  readonly mensaje = input.required<string>();
  readonly etiqueta = input('Escríbenos por WhatsApp');

  protected readonly url = computed(() => urlWhatsapp(this.mensaje()));
}
