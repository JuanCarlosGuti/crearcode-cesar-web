import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { COTIZACIONES } from '../../../contenido/cotizaciones';
import { Cotizacion, CotizacionesApi, EstadoCotizacion } from '../../api/cotizaciones-api';
import { CerrarSesionButton } from '../cerrar-sesion/cerrar-sesion';

const ESTADOS: readonly EstadoCotizacion[] = [
  'BORRADOR',
  'ENVIADA',
  'ACEPTADA',
  'RECHAZADA',
  'VENCIDA',
  'CANCELADA',
];

@Component({
  selector: 'app-pagina-listado-cotizaciones',
  templateUrl: './listado-cotizaciones.html',
  styleUrl: './listado-cotizaciones.scss',
  imports: [RouterLink, CerrarSesionButton],
})
export class ListadoCotizacionesPage implements OnInit {
  private readonly cotizacionesApi = inject(CotizacionesApi);

  protected readonly textos = COTIZACIONES.panel;
  protected readonly etiquetasDeEstado = COTIZACIONES.estados;
  protected readonly estados = ESTADOS;
  protected readonly filtro = signal<EstadoCotizacion | null>(null);
  protected readonly cotizaciones = signal<Cotizacion[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal(false);

  ngOnInit(): void {
    this.cargar();
  }

  protected filtrarPor(estado: string): void {
    this.filtro.set((estado || null) as EstadoCotizacion | null);
    this.cargar();
  }

  protected formatearFecha(iso: string): string {
    return new Date(iso).toLocaleDateString('es-CO', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  protected formatearPesos(valor: number): string {
    return valor.toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
  }

  private cargar(): void {
    this.cargando.set(true);
    this.error.set(false);
    this.cotizacionesApi.listar(this.filtro() ?? undefined).subscribe({
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
