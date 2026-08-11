import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { COTIZACIONES } from '../../../contenido/cotizaciones';
import { Cotizacion, CotizacionesApi, ItemPayload } from '../../api/cotizaciones-api';
import { CerrarSesionButton } from '../cerrar-sesion/cerrar-sesion';

interface ItemEditable {
  descripcion: string;
  cantidad: number;
  valorUnitario: number;
}

/**
 * Detalle y edición de una cotización (HU-44, HU-45). Los totales que se
 * muestran mientras se edita son una previsualización: los definitivos
 * llegan del servidor al guardar, que es quien los calcula.
 */
@Component({
  selector: 'app-pagina-detalle-cotizacion',
  templateUrl: './detalle-cotizacion.html',
  styleUrl: './detalle-cotizacion.scss',
  imports: [RouterLink, CerrarSesionButton],
})
export class DetalleCotizacionPage implements OnInit {
  readonly id = input.required<string>();

  private readonly cotizacionesApi = inject(CotizacionesApi);
  private readonly router = inject(Router);

  protected readonly textos = COTIZACIONES.detalle;
  protected readonly etiquetasDeEstado = COTIZACIONES.estados;

  protected readonly cotizacion = signal<Cotizacion | null>(null);
  protected readonly items = signal<ItemEditable[]>([]);
  protected readonly notas = signal('');
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly esBorrador = computed(() => this.cotizacion()?.estado === 'BORRADOR');
  protected readonly puedeEnviar = computed(() => this.esBorrador() && this.items().length > 0);

  /** Previsualización local mientras se edita (el servidor manda). */
  protected readonly subtotalPrevisto = computed(() =>
    this.items().reduce((suma, item) => suma + item.cantidad * item.valorUnitario, 0),
  );
  protected readonly totalPrevisto = computed(() => {
    const impuesto = this.cotizacion()?.impuestoPorcentaje ?? 0;
    return this.subtotalPrevisto() + Math.round((this.subtotalPrevisto() * impuesto) / 100);
  });

  ngOnInit(): void {
    this.cotizacionesApi.obtener(this.id()).subscribe({
      next: (cotizacion) => {
        this.aplicar(cotizacion);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(this.textos.errorGuardar);
        this.cargando.set(false);
      },
    });
  }

  protected agregarItem(): void {
    this.items.update((items) => [...items, { descripcion: '', cantidad: 1, valorUnitario: 0 }]);
  }

  protected quitarItem(indice: number): void {
    this.items.update((items) => items.filter((_, i) => i !== indice));
  }

  protected actualizarItem(indice: number, campo: keyof ItemEditable, valor: string): void {
    this.items.update((items) =>
      items.map((item, i) =>
        i === indice
          ? { ...item, [campo]: campo === 'descripcion' ? valor : Number(valor) || 0 }
          : item,
      ),
    );
  }

  protected guardar(): void {
    this.guardando.set(true);
    this.error.set(null);
    this.cotizacionesApi.editar(this.id(), this.aPayload(), this.notas() || null).subscribe({
      next: (cotizacion) => {
        this.aplicar(cotizacion);
        this.guardando.set(false);
      },
      error: () => {
        this.error.set(this.textos.errorGuardar);
        this.guardando.set(false);
      },
    });
  }

  protected enviar(): void {
    if (!confirm(this.textos.confirmarEnviar)) {
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    // Se guarda primero para que el cliente reciba lo que está en pantalla.
    this.cotizacionesApi.editar(this.id(), this.aPayload(), this.notas() || null).subscribe({
      next: () =>
        this.cotizacionesApi.enviar(this.id()).subscribe({
          next: (cotizacion) => {
            this.aplicar(cotizacion);
            this.enviando.set(false);
          },
          error: () => {
            this.error.set(this.textos.errorEnviar);
            this.enviando.set(false);
          },
        }),
      error: () => {
        this.error.set(this.textos.errorGuardar);
        this.enviando.set(false);
      },
    });
  }

  protected cancelar(): void {
    if (!confirm(this.textos.confirmarCancelar)) {
      return;
    }
    this.cotizacionesApi.cancelar(this.id()).subscribe({
      next: () => this.router.navigate(['/admin/cotizaciones']),
      error: () => this.error.set(this.textos.errorGuardar),
    });
  }

  protected descargar(): void {
    this.cotizacionesApi.descargar(this.id()).subscribe({
      next: (blob) => this.abrirDescarga(blob),
      error: () => this.error.set(this.textos.errorGuardar),
    });
  }

  protected formatearPesos(valor: number): string {
    return valor.toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
  }

  protected formatearFecha(iso: string): string {
    return new Date(iso).toLocaleDateString('es-CO', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  private abrirDescarga(blob: Blob): void {
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = `${this.cotizacion()?.numero ?? 'cotizacion'}.pdf`;
    enlace.click();
    URL.revokeObjectURL(url);
  }

  private aplicar(cotizacion: Cotizacion): void {
    this.cotizacion.set(cotizacion);
    this.items.set(
      cotizacion.items.map((item) => ({
        descripcion: item.descripcion,
        cantidad: item.cantidad,
        valorUnitario: item.valorUnitario,
      })),
    );
    this.notas.set(cotizacion.notas ?? '');
  }

  private aPayload(): ItemPayload[] {
    return this.items().map((item) => ({
      descripcion: item.descripcion,
      cantidad: item.cantidad,
      valorUnitario: item.valorUnitario,
    }));
  }
}
