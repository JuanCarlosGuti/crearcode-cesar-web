import { HttpErrorResponse } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';

import { AsistenteApi, MensajeEnviado, RolMensaje } from '../api/asistente-api';

export interface MensajeDeConversacion {
  rol: RolMensaje;
  texto: string;
  escalar?: boolean;
}

export type ErrorDeAsistente = 'limite-anonimo' | 'limite-registrado' | 'no-disponible';

const CLAVE_SESION_ANONIMA = 'crearcode-asistente-sesion';
// El backend acepta máximo 20 mensajes por petición: se envía la cola
// del historial (los turnos viejos dejan de dar contexto, no rompen).
const MAXIMO_MENSAJES_ENVIADOS = 20;

/**
 * Estado de la conversación con el asistente (F9): vive en memoria
 * mientras dura la visita (no se persiste, v1) y sobrevive a la
 * navegación entre páginas porque el servicio es singleton.
 */
@Injectable({ providedIn: 'root' })
export class ConversacionService {
  private readonly api = inject(AsistenteApi);
  private readonly esNavegador = isPlatformBrowser(inject(PLATFORM_ID));

  readonly mensajes = signal<MensajeDeConversacion[]>([]);
  readonly enviando = signal(false);
  readonly error = signal<ErrorDeAsistente | null>(null);

  enviar(texto: string): void {
    const pregunta = texto.trim();
    if (pregunta.length === 0 || this.enviando()) {
      return;
    }

    this.error.set(null);
    this.mensajes.update((mensajes) => [...mensajes, { rol: 'USUARIO', texto: pregunta }]);
    this.enviando.set(true);

    const historial: MensajeEnviado[] = this.mensajes()
      .slice(-MAXIMO_MENSAJES_ENVIADOS)
      .map(({ rol, texto: t }) => ({ rol, texto: t }));

    this.api.enviar(historial, this.idSesionAnonima()).subscribe({
      next: (respuesta) => {
        this.enviando.set(false);
        this.mensajes.update((mensajes) => [
          ...mensajes,
          { rol: 'ASISTENTE', texto: respuesta.texto, escalar: respuesta.escalarAHumano },
        ]);
      },
      error: (error: unknown) => {
        this.enviando.set(false);
        this.error.set(this.codigoDesde(error));
      },
    });
  }

  private codigoDesde(error: unknown): ErrorDeAsistente {
    if (error instanceof HttpErrorResponse) {
      const codigo = (error.error as { codigo?: string } | null)?.codigo;
      if (codigo === 'limite-anonimo' || codigo === 'limite-registrado') {
        return codigo;
      }
    }
    return 'no-disponible';
  }

  private idSesionAnonima(): string {
    if (!this.esNavegador) {
      return 'ssr';
    }
    let id = sessionStorage.getItem(CLAVE_SESION_ANONIMA);
    if (id === null) {
      id = crypto.randomUUID();
      sessionStorage.setItem(CLAVE_SESION_ANONIMA, id);
    }
    return id;
  }
}
