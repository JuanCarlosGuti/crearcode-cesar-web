import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CONSENTIMIENTO } from '../../../contenido/analitica';
import { Analitica } from '../../nucleo/analitica';

/**
 * Banner de consentimiento de las cookies de medición.
 *
 * <p>
 * Solo aparece si hay medición configurada y el visitante no ha
 * respondido. Las dos opciones pesan lo mismo: un «Rechazar» escondido
 * o disfrazado de enlace no es un consentimiento libre, y este banner
 * existe precisamente para que lo sea.
 *
 * <p>
 * No es un modal ni atrapa el foco: no bloquea el contenido, así que
 * quien no quiera responder puede seguir leyendo. Es una región
 * anunciada, no una puerta.
 */
@Component({
  selector: 'app-consentimiento-cookies',
  imports: [RouterLink],
  template: `
    @if (analitica.debePreguntar()) {
      <div class="consentimiento" role="region" [attr.aria-label]="textos.titulo">
        <p class="consentimiento__texto">
          {{ textos.texto }}
          <a routerLink="/legales/politica-de-datos">{{ textos.enlacePolitica }}</a>
        </p>
        <div class="consentimiento__acciones">
          <button type="button" class="boton boton-secundario" (click)="analitica.rechazar()">
            {{ textos.rechazar }}
          </button>
          <button type="button" class="boton boton-primario" (click)="analitica.conceder()">
            {{ textos.aceptar }}
          </button>
        </div>
      </div>
    }
  `,
  styleUrl: './consentimiento-cookies.scss',
})
export class ConsentimientoCookies {
  protected readonly analitica = inject(Analitica);
  protected readonly textos = CONSENTIMIENTO;
}
