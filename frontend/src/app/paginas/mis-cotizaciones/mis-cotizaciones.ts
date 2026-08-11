import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { COTIZACIONES } from '../../../contenido/cotizaciones';
import { Cotizacion, MisCotizacionesApi } from '../../api/cotizaciones-api';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

/**
 * Las cotizaciones del cliente dentro de su cuenta (HU-46). Es la
 * primera vez que la cuenta sirve para el negocio real y no solo para
 * los cupos de las herramientas de IA.
 */
@Component({
  selector: 'app-pagina-mis-cotizaciones',
  templateUrl: './mis-cotizaciones.html',
  styleUrl: './mis-cotizaciones.scss',
  imports: [RouterLink],
})
export class MisCotizacionesPage implements OnInit {
  private readonly api = inject(MisCotizacionesApi);

  protected readonly textos = COTIZACIONES.cliente;
  protected readonly etiquetasDeEstado = COTIZACIONES.estados;

  protected readonly cotizaciones = signal<Cotizacion[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal(false);
  protected readonly seleccionada = signal<Cotizacion | null>(null);
  protected readonly respondiendo = signal(false);
  protected readonly errorAlResponder = signal(false);

  protected readonly puedeResponder = computed(() => {
    const cotizacion = this.seleccionada();
    if (!cotizacion || cotizacion.estado !== 'ENVIADA') {
      return false;
    }
    return new Date(cotizacion.validaHasta).getTime() >= Date.now();
  });

  constructor() {
    establecerMetadatosDePagina(() => ({
      titulo: 'Mis cotizaciones — Crear Code Cesar',
      descripcion: 'Consulta y responde las cotizaciones que te enviamos.',
      ruta: '/mi-cuenta/cotizaciones',
    }));
  }

  ngOnInit(): void {
    this.cargar();
  }

  protected seleccionar(cotizacion: Cotizacion): void {
    this.seleccionada.set(cotizacion);
    this.errorAlResponder.set(false);
  }

  protected volverAlListado(): void {
    this.seleccionada.set(null);
  }

  protected aceptar(): void {
    if (!confirm(this.textos.confirmarAceptar)) {
      return;
    }
    this.responder((id) => this.api.aceptar(id));
  }

  protected rechazar(): void {
    if (!confirm(this.textos.confirmarRechazar)) {
      return;
    }
    this.responder((id) => this.api.rechazar(id));
  }

  protected descargar(cotizacion: Cotizacion): void {
    this.api.descargar(cotizacion.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `${cotizacion.numero ?? 'cotizacion'}.pdf`;
        enlace.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.errorAlResponder.set(true),
    });
  }

  protected estaVencida(cotizacion: Cotizacion): boolean {
    return (
      cotizacion.estado === 'VENCIDA' ||
      (cotizacion.estado === 'ENVIADA' && new Date(cotizacion.validaHasta).getTime() < Date.now())
    );
  }

  protected formatearPesos(valor: number): string {
    return valor.toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
  }

  protected formatearFecha(iso: string): string {
    return new Date(iso).toLocaleDateString('es-CO', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  private responder(accion: (id: string) => ReturnType<MisCotizacionesApi['aceptar']>): void {
    const cotizacion = this.seleccionada();
    if (!cotizacion) {
      return;
    }
    this.respondiendo.set(true);
    this.errorAlResponder.set(false);
    accion(cotizacion.id).subscribe({
      next: (actualizada) => {
        this.seleccionada.set(actualizada);
        this.respondiendo.set(false);
        this.cargar();
      },
      error: () => {
        this.errorAlResponder.set(true);
        this.respondiendo.set(false);
      },
    });
  }

  private cargar(): void {
    this.cargando.set(true);
    this.error.set(false);
    this.api.listar().subscribe({
      next: (cotizaciones) => {
        this.cotizaciones.set([...cotizaciones].sort((a, b) => b.creadaEn.localeCompare(a.creadaEn)));
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }
}
