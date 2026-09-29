import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { COTIZADOR } from '../../../contenido/cotizador';
import { Analitica } from '../../nucleo/analitica';
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

  private readonly analitica = inject(Analitica);

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
  // El tipo tambien manda: antes el rango salia solo del alcance y una
  // pagina web arrancaba en el mismo piso que un sistema (P1-2a).
  protected readonly rango = computed(
    () => COTIZADOR.rangosPorTipoYAlcance[this.respuestas()['tipo']]?.[this.respuestas()['alcance']] ?? '',
  );
  protected readonly notaUrgencia = computed(
    () => COTIZADOR.notaPorUrgencia[this.respuestas()['urgencia']] ?? '',
  );
  protected readonly resumen = computed(() =>
    COTIZADOR.pasos
      .map((p) => this.respuestas()[p.clave])
      .filter(Boolean)
      .join(' · '),
  );
  protected readonly mensajeWhatsapp = computed(
    () =>
      `Hola, usé el cotizador de la página. Mi proyecto: ${this.resumen()}. Me salió un rango de ${this.rango()}. ¿Podemos hablar del alcance?`,
  );

  protected elegir(opcion: string): void {
    const clave = this.paso().clave;
    if (this.indice() === 0) {
      this.analitica.registrar('tool_start', { herramienta: 'cotizador' });
    }
    this.respuestas.update((r) => ({ ...r, [clave]: opcion }));
    this.indice.update((i) => i + 1);
    if (this.completado()) {
      // El rango, no la cifra de nadie: sirve para saber que tipo de
      // proyecto llega al sitio y con que alcance.
      this.analitica.registrar('quote_generated', {
        tipo: this.respuestas()['tipo'] ?? '',
        alcance: this.respuestas()['alcance'] ?? '',
        rango: this.rango(),
      });
    }
  }

  protected readonly puedeRetroceder = computed(() => this.indice() > 0);

  // Vuelve un paso sin borrar lo elegido: si el visitante cambia una
  // respuesta, la nueva pisa a la vieja al elegir de nuevo (QA4 de la
  // auditoria: antes solo existia "Empezar de nuevo", al final).
  protected retroceder(): void {
    this.indice.update((i) => Math.max(0, i - 1));
  }

  protected reiniciar(): void {
    this.indice.set(0);
    this.respuestas.set({});
  }
}
