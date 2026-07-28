import { Component, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map } from 'rxjs';

import { ASISTENTE } from '../../../contenido/asistente';
import { ConversacionService } from '../../nucleo/conversacion';
import { SesionService } from '../../nucleo/sesion';
import { mensajeWhatsappParaRuta } from '../../layout/mensaje-whatsapp-por-ruta';
import { WhatsappCta } from '../whatsapp-cta/whatsapp-cta';

/**
 * Widget flotante del asistente IA (F9, HU-36 a HU-38). El estado de
 * la conversación vive en ConversacionService (sobrevive a la
 * navegación); este componente solo maneja la interfaz: panel
 * abierto/cerrado, borrador, escalamiento y avisos de límite.
 */
@Component({
  selector: 'app-chat-asistente',
  templateUrl: './chat-asistente.html',
  styleUrl: './chat-asistente.scss',
  imports: [RouterLink, WhatsappCta],
})
export class ChatAsistente {
  private readonly router = inject(Router);
  private readonly urlActual = toSignal(
    this.router.events.pipe(
      filter((evento) => evento instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );

  protected readonly conversacion = inject(ConversacionService);
  protected readonly sesion = inject(SesionService);
  protected readonly textos = ASISTENTE;
  protected readonly abierto = signal(false);
  protected readonly mensajeWhatsapp = computed(() => mensajeWhatsappParaRuta(this.urlActual()));

  private readonly campoDePregunta = viewChild<ElementRef<HTMLInputElement>>('campoPregunta');

  protected alternarPanel(): void {
    this.abierto.update((abierto) => !abierto);
    if (this.abierto()) {
      // El panel acaba de entrar al DOM: el foco va al campo en cuanto
      // exista (siguiente microtask tras el render).
      queueMicrotask(() => this.campoDePregunta()?.nativeElement.focus());
    }
  }

  protected cerrarPanel(): void {
    this.abierto.set(false);
  }

  protected enviar(evento: Event): void {
    evento.preventDefault();
    const campo = this.campoDePregunta()?.nativeElement;
    if (!campo) {
      return;
    }
    this.conversacion.enviar(campo.value);
    campo.value = '';
  }

  protected enviarSugerencia(sugerencia: string): void {
    this.conversacion.enviar(sugerencia);
  }
}
