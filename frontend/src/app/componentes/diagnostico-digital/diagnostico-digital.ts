import { HttpErrorResponse } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Component, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { DIAGNOSTICO } from '../../../contenido/diagnostico';
import { mensajeWhatsappParaRuta } from '../../layout/mensaje-whatsapp-por-ruta';
import {
  DiagnosticoApi,
  InformeDeDiagnostico,
  ParDeDiagnostico,
} from '../../api/diagnostico-api';
import { WhatsappCta } from '../whatsapp-cta/whatsapp-cta';

type ErrorDeDiagnostico = 'limite-anonimo' | 'limite-registrado' | 'no-disponible' | null;

const CLAVE_SESION_ANONIMA = 'crearcode-asistente-sesion';

/**
 * Quiz del diagnóstico digital (F10c, HU-41), inline en /herramientas.
 * El informe se muestra EN PANTALLA (decisión 13): el envío por correo
 * llega con el MVP. Un fallo del proveedor conserva las respuestas
 * para reintentar sin repetir el quiz.
 */
@Component({
  selector: 'app-diagnostico-digital',
  templateUrl: './diagnostico-digital.html',
  styleUrl: './diagnostico-digital.scss',
  imports: [RouterLink, WhatsappCta],
})
export class DiagnosticoDigital {
  private readonly api = inject(DiagnosticoApi);
  private readonly esNavegador = isPlatformBrowser(inject(PLATFORM_ID));

  protected readonly textos = DIAGNOSTICO;
  protected readonly mensajeWhatsapp = mensajeWhatsappParaRuta('/herramientas');

  private readonly indice = signal(0);
  private readonly respuestas = signal<ParDeDiagnostico[]>([]);
  protected readonly analizando = signal(false);
  protected readonly informe = signal<InformeDeDiagnostico | null>(null);
  protected readonly error = signal<ErrorDeDiagnostico>(null);

  protected readonly enQuiz = computed(
    () => this.informe() === null && !this.analizando() && this.error() === null,
  );
  protected readonly pregunta = computed(
    () => DIAGNOSTICO.preguntas[Math.min(this.indice(), DIAGNOSTICO.preguntas.length - 1)],
  );
  protected readonly etiquetaProgreso = computed(() =>
    this.informe() !== null
      ? 'radiografía lista'
      : `${Math.min(this.indice() + 1, DIAGNOSTICO.preguntas.length)} de ${DIAGNOSTICO.preguntas.length}`,
  );
  protected readonly porcentaje = computed(() =>
    Math.round((Math.min(this.indice(), DIAGNOSTICO.preguntas.length) / DIAGNOSTICO.preguntas.length) * 100),
  );

  protected responder(opcion: string): void {
    if (!this.enQuiz()) {
      return;
    }
    this.respuestas.update((r) => [...r, { pregunta: this.pregunta().pregunta, respuesta: opcion }]);
    this.indice.update((i) => i + 1);
    if (this.indice() >= DIAGNOSTICO.preguntas.length) {
      this.generar();
    }
  }

  protected reintentar(): void {
    this.generar();
  }

  protected reiniciar(): void {
    this.indice.set(0);
    this.respuestas.set([]);
    this.informe.set(null);
    this.error.set(null);
    this.analizando.set(false);
  }

  private generar(): void {
    this.error.set(null);
    this.analizando.set(true);
    this.api.generar(this.respuestas(), this.idSesionAnonima()).subscribe({
      next: (informe) => {
        this.analizando.set(false);
        this.informe.set(informe);
      },
      error: (error: unknown) => {
        this.analizando.set(false);
        this.error.set(this.codigoDesde(error));
      },
    });
  }

  private codigoDesde(error: unknown): ErrorDeDiagnostico {
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
