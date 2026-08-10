import { HttpErrorResponse } from '@angular/common/http';
import { Component, afterNextRender, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { DEMO_DISENO } from '../../../contenido/demo-diseno';
import { BocetoDeDemo, DemoApi } from '../../api/demo-api';
import { SesionService } from '../../nucleo/sesion';
import { mensajeWhatsappParaRuta } from '../../layout/mensaje-whatsapp-por-ruta';
import { WhatsappCta } from '../whatsapp-cta/whatsapp-cta';

type ErrorDeDemo = 'limite' | 'no-disponible' | null;

/**
 * Demo de diseño con IA (F10d, HU-42), inline en /herramientas. SOLO
 * registrados: el estado bloqueado (también el del prerender) invita a
 * crear cuenta; tras hidratar, la sesión revela el formulario. La
 * imagen se muestra SOLO como <img> con data URI — nunca HTML.
 */
@Component({
  selector: 'app-demo-diseno',
  templateUrl: './demo-diseno.html',
  styleUrl: './demo-diseno.scss',
  imports: [RouterLink, WhatsappCta],
})
export class DemoDiseno {
  private readonly api = inject(DemoApi);
  protected readonly sesion = inject(SesionService);

  protected readonly textos = DEMO_DISENO;
  protected readonly mensajeWhatsapp = mensajeWhatsappParaRuta('/herramientas');

  protected readonly hidratado = signal(false);
  protected readonly sector = signal('');
  protected readonly queHace = signal('');
  protected readonly queNecesita = signal('');
  protected readonly generando = signal(false);
  protected readonly boceto = signal<BocetoDeDemo | null>(null);
  protected readonly error = signal<ErrorDeDemo>(null);
  protected readonly variacionUsada = signal(false);

  protected readonly desbloqueado = computed(() => this.hidratado() && this.sesion.estaAutenticado());
  protected readonly puedeGenerar = computed(
    () =>
      !this.generando() &&
      this.sector().trim().length > 0 &&
      this.queHace().trim().length > 0 &&
      this.queNecesita().trim().length > 0,
  );
  protected readonly imagenSrc = computed(() => {
    const resultado = this.boceto();
    return resultado ? `data:${resultado.tipoMime};base64,${resultado.imagenBase64}` : '';
  });

  constructor() {
    afterNextRender(() => this.hidratado.set(true));
  }

  protected actualizar(campo: 'sector' | 'queHace' | 'queNecesita', evento: Event): void {
    const valor = (evento.target as HTMLInputElement | HTMLTextAreaElement).value;
    this[campo].set(valor);
  }

  protected generar(): void {
    if (!this.puedeGenerar()) {
      return;
    }
    this.lanzar(false);
  }

  protected pedirVariacion(): void {
    if (this.variacionUsada() || this.generando()) {
      return;
    }
    this.lanzar(true);
  }

  protected reiniciar(): void {
    this.boceto.set(null);
    this.error.set(null);
    this.variacionUsada.set(false);
    this.sector.set('');
    this.queHace.set('');
    this.queNecesita.set('');
  }

  protected reintentar(): void {
    this.lanzar(this.variacionUsada());
  }

  private lanzar(esVariacion: boolean): void {
    this.error.set(null);
    this.generando.set(true);
    this.api
      .generar({
        sector: this.sector().trim(),
        queHace: this.queHace().trim(),
        queNecesita: this.queNecesita().trim(),
      })
      .subscribe({
        next: (resultado) => {
          this.generando.set(false);
          this.boceto.set(resultado);
          if (esVariacion) {
            this.variacionUsada.set(true);
          }
        },
        error: (error: unknown) => {
          this.generando.set(false);
          this.error.set(this.codigoDesde(error));
        },
      });
  }

  private codigoDesde(error: unknown): ErrorDeDemo {
    if (error instanceof HttpErrorResponse) {
      const codigo = (error.error as { codigo?: string } | null)?.codigo;
      if (codigo === 'limite-registrado' || codigo === 'limite-anonimo') {
        return 'limite';
      }
    }
    return 'no-disponible';
  }
}
