import { Injectable, signal } from '@angular/core';

/**
 * Puente entre las páginas y el widget del asistente (F10e, ISS-133):
 * las preguntas sugeridas de la Home abren el panel flotante y envían
 * la pregunta, sin acoplar la página al componente del chat.
 */
@Injectable({ providedIn: 'root' })
export class AsistenteUiService {
  /** Contador de solicitudes de apertura (0 = ninguna todavía). */
  readonly aperturas = signal(0);
  private preguntaPendiente: string | null = null;

  abrir(pregunta?: string): void {
    this.preguntaPendiente = pregunta ?? null;
    this.aperturas.update((n) => n + 1);
  }

  /** Entrega la pregunta pendiente una sola vez (o null). */
  consumirPregunta(): string | null {
    const pregunta = this.preguntaPendiente;
    this.preguntaPendiente = null;
    return pregunta;
  }
}
