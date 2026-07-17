import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { EstadoSolicitud, Solicitud, SolicitudesApi } from '../../api/solicitudes-api';

const TRANSICIONES_VALIDAS: Record<EstadoSolicitud, readonly EstadoSolicitud[]> = {
  NUEVA: ['CONTACTADA', 'DESCARTADA'],
  CONTACTADA: ['CONVERTIDA', 'DESCARTADA'],
  CONVERTIDA: [],
  DESCARTADA: [],
};

const ETIQUETAS_TRANSICION: Record<EstadoSolicitud, string> = {
  NUEVA: '',
  CONTACTADA: 'Marcar como contactada',
  CONVERTIDA: 'Marcar como convertida',
  DESCARTADA: 'Descartar',
};

@Component({
  selector: 'app-pagina-detalle-solicitud',
  templateUrl: './detalle-solicitud.html',
  styleUrl: './detalle-solicitud.scss',
  imports: [RouterLink],
})
export class DetalleSolicitudPage implements OnInit {
  private readonly solicitudesApi = inject(SolicitudesApi);

  readonly id = input.required<string>();

  protected readonly solicitud = signal<Solicitud | null>(null);
  protected readonly cargando = signal(true);
  protected readonly cambiando = signal(false);
  protected readonly confirmando = signal<EstadoSolicitud | null>(null);
  protected readonly error = signal(false);

  protected readonly transicionesDisponibles = computed(() => {
    const actual = this.solicitud()?.estado;
    return actual ? TRANSICIONES_VALIDAS[actual] : [];
  });

  ngOnInit(): void {
    this.cargando.set(true);
    this.solicitudesApi.listar().subscribe({
      next: (solicitudes) => {
        this.solicitud.set(solicitudes.find((s) => s.id === this.id()) ?? null);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false),
    });
  }

  protected etiquetaTransicion(estado: EstadoSolicitud): string {
    return ETIQUETAS_TRANSICION[estado];
  }

  protected formatearFecha(iso: string): string {
    return new Date(iso).toLocaleDateString('es-CO', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  protected pedirConfirmacion(estado: EstadoSolicitud): void {
    this.confirmando.set(estado);
  }

  protected cancelarConfirmacion(): void {
    this.confirmando.set(null);
  }

  protected confirmarCambio(): void {
    const nuevoEstado = this.confirmando();
    const actual = this.solicitud();
    if (!nuevoEstado || !actual) {
      return;
    }

    this.cambiando.set(true);
    this.error.set(false);
    this.solicitudesApi.cambiarEstado(actual.id, nuevoEstado).subscribe({
      next: () => {
        this.solicitud.set({ ...actual, estado: nuevoEstado });
        this.cambiando.set(false);
        this.confirmando.set(null);
      },
      error: () => {
        this.cambiando.set(false);
        this.error.set(true);
      },
    });
  }
}
