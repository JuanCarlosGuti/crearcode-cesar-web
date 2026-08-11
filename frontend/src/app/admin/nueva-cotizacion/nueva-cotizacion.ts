import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { COTIZACIONES } from '../../../contenido/cotizaciones';
import { CotizacionesApi } from '../../api/cotizaciones-api';
import { CerrarSesionButton } from '../cerrar-sesion/cerrar-sesion';

/**
 * Abre el borrador con los datos del cliente (HU-44). Los ítems se
 * agregan después, en el detalle: así el formulario de apertura es corto
 * y el trabajo fino se hace donde se ven los totales.
 *
 * <p>Acepta `?solicitud=<id>` para cotizar desde un lead: en ese caso el
 * servidor toma los datos del contacto de la solicitud.
 */
@Component({
  selector: 'app-pagina-nueva-cotizacion',
  templateUrl: './nueva-cotizacion.html',
  styleUrl: './nueva-cotizacion.scss',
  imports: [RouterLink, CerrarSesionButton],
})
export class NuevaCotizacionPage {
  private readonly cotizacionesApi = inject(CotizacionesApi);
  private readonly router = inject(Router);

  protected readonly textos = COTIZACIONES.detalle;

  protected readonly origenSolicitudId = signal<string | null>(null);
  protected readonly nombre = signal('');
  protected readonly correo = signal('');
  protected readonly telefono = signal('');
  protected readonly identificacion = signal('');
  // Espeja el default del backend (19%): el servidor manda, pero el
  // formulario no debe proponer 0 y sorprender al fundador.
  protected readonly impuesto = signal(19);
  protected readonly diasDeValidez = signal(15);
  protected readonly creando = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    const desdeLead = new URLSearchParams(globalThis.location?.search ?? '').get('solicitud');
    this.origenSolicitudId.set(desdeLead);
  }

  protected get vieneDeUnLead(): boolean {
    return this.origenSolicitudId() !== null;
  }

  protected crear(evento: Event): void {
    evento.preventDefault();
    this.creando.set(true);
    this.error.set(null);

    this.cotizacionesApi
      .abrir({
        origenSolicitudId: this.origenSolicitudId(),
        cliente: this.vieneDeUnLead
          ? null
          : {
              nombre: this.nombre(),
              correo: this.correo(),
              telefono: this.telefono() || null,
              identificacion: this.identificacion() || null,
            },
        impuestoPorcentaje: this.impuesto(),
        diasDeValidez: this.diasDeValidez(),
        items: [],
      })
      .subscribe({
        next: (cotizacion) => this.router.navigate(['/admin/cotizaciones', cotizacion.id]),
        error: () => {
          this.error.set(this.textos.errorGuardar);
          this.creando.set(false);
        },
      });
  }
}
