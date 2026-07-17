import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { EstadoSolicitud, Solicitud, SolicitudesApi } from '../../api/solicitudes-api';
import { CerrarSesionButton } from '../cerrar-sesion/cerrar-sesion';

const ESTADOS: readonly EstadoSolicitud[] = ['NUEVA', 'CONTACTADA', 'CONVERTIDA', 'DESCARTADA'];

@Component({
  selector: 'app-pagina-listado-solicitudes',
  templateUrl: './listado-solicitudes.html',
  styleUrl: './listado-solicitudes.scss',
  imports: [RouterLink, CerrarSesionButton],
})
export class ListadoSolicitudesPage implements OnInit {
  private readonly solicitudesApi = inject(SolicitudesApi);

  protected readonly estados = ESTADOS;
  protected readonly filtro = signal<EstadoSolicitud | null>(null);
  protected readonly solicitudes = signal<Solicitud[]>([]);
  protected readonly cargando = signal(true);

  ngOnInit(): void {
    this.cargar();
  }

  protected filtrarPor(estado: string): void {
    this.filtro.set((estado || null) as EstadoSolicitud | null);
    this.cargar();
  }

  protected formatearFecha(iso: string): string {
    return new Date(iso).toLocaleDateString('es-CO', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  private cargar(): void {
    this.cargando.set(true);
    this.solicitudesApi.listar(this.filtro() ?? undefined).subscribe({
      next: (solicitudes) => {
        this.solicitudes.set(this.ordenarPorFechaDesc(solicitudes));
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false),
    });
  }

  private ordenarPorFechaDesc(solicitudes: Solicitud[]): Solicitud[] {
    return [...solicitudes].sort((a, b) => b.fechaCreacion.localeCompare(a.fechaCreacion));
  }
}
