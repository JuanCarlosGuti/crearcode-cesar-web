import { Component, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { COTIZADOR } from '../../../contenido/cotizador';
import { WhatsappCta } from '../whatsapp-cta/whatsapp-cta';

/**
 * Cotizador interactivo (F10a, HU-39): wizard de 3 pasos que termina
 * en un RANGO orientativo — nunca cifras exactas. Sin backend: las
 * reglas viven en contenido/cotizador.ts.
 */
@Component({
  selector: 'app-cotizador',
  templateUrl: './cotizador.html',
  styleUrl: './cotizador.scss',
  imports: [RouterLink, WhatsappCta],
})
export class Cotizador {
  protected readonly textos = COTIZADOR;

  private readonly indice = signal(0);
  private readonly respuestas = signal<Record<string, string>>({});

  protected readonly completado = computed(() => this.indice() >= COTIZADOR.pasos.length);
  protected readonly paso = computed(
    () => COTIZADOR.pasos[Math.min(this.indice(), COTIZADOR.pasos.length - 1)],
  );
  protected readonly etiquetaProgreso = computed(() =>
    this.completado() ? 'listo' : `${this.indice() + 1} de ${COTIZADOR.pasos.length}`,
  );
  protected readonly porcentaje = computed(() =>
    Math.round((Math.min(this.indice(), COTIZADOR.pasos.length) / COTIZADOR.pasos.length) * 100),
  );
  protected readonly rango = computed(
    () => COTIZADOR.rangosPorAlcance[this.respuestas()['alcance']] ?? '',
  );
  protected readonly resumen = computed(() =>
    COTIZADOR.pasos
      .map((p) => this.respuestas()[p.clave])
      .filter(Boolean)
      .join(' · '),
  );
  protected readonly mensajeWhatsapp = computed(
    () =>
      `Hola, usé el cotizador de la página. Mi proyecto: ${this.resumen()}. ¿Podemos hablar del alcance?`,
  );

  protected elegir(opcion: string): void {
    const clave = this.paso().clave;
    this.respuestas.update((r) => ({ ...r, [clave]: opcion }));
    this.indice.update((i) => i + 1);
  }

  protected reiniciar(): void {
    this.indice.set(0);
    this.respuestas.set({});
  }
}
