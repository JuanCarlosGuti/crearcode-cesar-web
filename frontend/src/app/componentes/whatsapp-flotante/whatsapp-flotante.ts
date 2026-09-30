import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { filter, map } from 'rxjs';

import { urlWhatsapp } from '../../../contenido/empresa';
import { mensajeWhatsappParaRuta } from '../../layout/mensaje-whatsapp-por-ruta';
import { Analitica } from '../../nucleo/analitica';

/**
 * Botón flotante de WhatsApp, solo en móvil.
 *
 * <p>
 * En pantallas pequeñas el WhatsApp estaba escondido detrás del menú
 * hamburguesa o al final del pie: para escribir había que buscarlo
 * (auditoría del 28 sep 2026, P1-4). En escritorio no hace falta —
 * está en el header, en el hero y en el cierre—, así que aparece solo
 * bajo el mismo corte de 60rem que usa el header para plegarse; con el
 * corte de 768px de la auditoría, la franja entre 768 y 960 px se
 * quedaba en modo móvil y sin botón.
 *
 * <p>
 * Se apila ENCIMA de la burbuja del asistente, que ocupa fija esa misma
 * esquina: el desplazamiento es la altura de la burbuja más el hueco,
 * y el z-index queda por debajo para que el panel del chat, al abrirse,
 * lo tape en vez de pelearse con él.
 */
@Component({
  selector: 'app-whatsapp-flotante',
  template: `
    <a
      class="whatsapp-flotante"
      [href]="url()"
      target="_blank"
      rel="noopener"
      aria-label="Escríbenos por WhatsApp"
      (click)="registrarClic()"
    >
      <svg viewBox="0 0 24 24" width="26" height="26" aria-hidden="true" focusable="false" fill="currentColor">
        <path
          d="M17.5 14.4c-.3-.2-1.7-.9-2-1-.3-.1-.5-.2-.7.1-.2.3-.7 1-.9 1.2-.2.2-.3.2-.6.1-.3-.2-1.3-.5-2.4-1.5-.9-.8-1.5-1.8-1.7-2.1-.2-.3 0-.5.1-.6l.5-.5c.1-.2.2-.3.3-.5 0-.2 0-.4 0-.5 0-.2-.7-1.6-.9-2.2-.2-.6-.5-.5-.7-.5h-.6c-.2 0-.5.1-.8.4-.3.3-1 1-1 2.5s1.1 2.9 1.2 3.1c.2.2 2.1 3.2 5.1 4.4.7.3 1.3.5 1.7.6.7.2 1.4.2 1.9.1.6-.1 1.7-.7 2-1.4.2-.7.2-1.3.2-1.4-.1-.1-.3-.2-.6-.3zM12 2a10 10 0 0 0-8.5 15.3L2 22l4.8-1.5A10 10 0 1 0 12 2zm0 18.2c-1.6 0-3.1-.4-4.4-1.2l-.3-.2-2.9.9.9-2.8-.2-.3A8.2 8.2 0 1 1 12 20.2z"
        />
      </svg>
    </a>
  `,
  styleUrl: './whatsapp-flotante.scss',
})
export class WhatsappFlotante {
  private readonly router = inject(Router);
  private readonly analitica = inject(Analitica);
  private readonly urlActual = toSignal(
    this.router.events.pipe(
      filter((evento) => evento instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );

  protected readonly url = computed(() => urlWhatsapp(mensajeWhatsappParaRuta(this.urlActual())));

  protected registrarClic(): void {
    this.analitica.registrar('whatsapp_click', { origen: 'flotante-movil' });
  }
}
