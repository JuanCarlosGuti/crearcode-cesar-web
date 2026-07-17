import { Component, input, signal } from '@angular/core';

import { PreguntaFrecuente } from '../../../contenido/tipos';

@Component({
  selector: 'app-faq',
  templateUrl: './faq.html',
  styleUrl: './faq.scss',
})
export class Faq {
  readonly preguntas = input.required<readonly PreguntaFrecuente[]>();

  protected readonly indiceAbierto = signal<number | null>(null);

  alternar(indice: number): void {
    this.indiceAbierto.update((actual) => (actual === indice ? null : indice));
  }
}
