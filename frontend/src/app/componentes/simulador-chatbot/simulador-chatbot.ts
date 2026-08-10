import { HttpErrorResponse } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Component, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { SIMULADOR } from '../../../contenido/simulador';
import { MensajeEnviado } from '../../api/asistente-api';
import { SimuladorApi } from '../../api/simulador-api';

type ErrorDeSimulador = 'limite-anonimo' | 'limite-registrado' | 'no-disponible' | null;

const CLAVE_SESION_ANONIMA = 'crearcode-asistente-sesion';
const MAXIMO_MENSAJES_ENVIADOS = 20;

/**
 * Simulador "un chatbot para tu negocio" (F10b, HU-40), inline en
 * /herramientas (decisión 11). El estado vive en el componente: la
 * conversación es un demo puntual, no necesita sobrevivir a la
 * navegación.
 */
@Component({
  selector: 'app-simulador-chatbot',
  templateUrl: './simulador-chatbot.html',
  styleUrl: './simulador-chatbot.scss',
  imports: [RouterLink],
})
export class SimuladorChatbot {
  private readonly api = inject(SimuladorApi);
  private readonly esNavegador = isPlatformBrowser(inject(PLATFORM_ID));

  protected readonly textos = SIMULADOR;
  protected readonly nombre = signal('');
  protected readonly rubro = signal('');
  protected readonly borrador = signal('');
  protected readonly mensajes = signal<MensajeEnviado[]>([]);
  protected readonly enviando = signal(false);
  protected readonly error = signal<ErrorDeSimulador>(null);
  protected readonly avisoNegocio = signal(false);

  protected readonly tituloChat = computed(() =>
    this.nombre().trim().length > 0
      ? this.nombre().trim() + SIMULADOR.sufijoTituloChat
      : SIMULADOR.tituloChatVacio,
  );

  protected actualizarNombre(evento: Event): void {
    this.nombre.set((evento.target as HTMLInputElement).value);
  }

  protected actualizarRubro(evento: Event): void {
    this.rubro.set((evento.target as HTMLInputElement).value);
  }

  protected actualizarBorrador(evento: Event): void {
    this.borrador.set((evento.target as HTMLInputElement).value);
  }

  protected enviar(evento: Event): void {
    evento.preventDefault();
    const texto = this.borrador().trim();
    if (texto.length === 0 || this.enviando()) {
      return;
    }
    if (this.nombre().trim().length === 0 || this.rubro().trim().length === 0) {
      this.avisoNegocio.set(true);
      return;
    }

    this.avisoNegocio.set(false);
    this.error.set(null);
    this.mensajes.update((m) => [...m, { rol: 'USUARIO', texto }]);
    this.borrador.set('');
    this.enviando.set(true);

    const historial = this.mensajes().slice(-MAXIMO_MENSAJES_ENVIADOS);
    const negocio = { nombre: this.nombre().trim(), rubro: this.rubro().trim() };

    this.api.enviar(negocio, historial, this.idSesionAnonima()).subscribe({
      next: (respuesta) => {
        this.enviando.set(false);
        this.mensajes.update((m) => [...m, { rol: 'ASISTENTE', texto: respuesta.texto }]);
      },
      error: (error: unknown) => {
        this.enviando.set(false);
        this.error.set(this.codigoDesde(error));
      },
    });
  }

  private codigoDesde(error: unknown): ErrorDeSimulador {
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
