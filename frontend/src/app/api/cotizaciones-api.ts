import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export type EstadoCotizacion = 'BORRADOR' | 'ENVIADA' | 'ACEPTADA' | 'RECHAZADA' | 'VENCIDA' | 'CANCELADA';

export interface ItemDeCotizacion {
  descripcion: string;
  cantidad: number;
  valorUnitario: number;
  subtotal: number;
}

export interface Cotizacion {
  id: string;
  numero: string | null;
  estado: EstadoCotizacion;
  origenSolicitudId: string | null;
  clienteNombre: string;
  clienteCorreo: string;
  clienteTelefono: string | null;
  clienteIdentificacion: string | null;
  impuestoPorcentaje: number;
  notas: string | null;
  creadaEn: string;
  validaHasta: string;
  enviadaEn: string | null;
  respondidaEn: string | null;
  items: ItemDeCotizacion[];
  /** Calculados en el servidor: aquí solo se muestran. */
  subtotal: number;
  impuesto: number;
  total: number;
}

export interface ItemPayload {
  descripcion: string;
  cantidad: number;
  valorUnitario: number;
}

export interface NuevoBorradorPayload {
  origenSolicitudId?: string | null;
  cliente?: {
    nombre: string;
    correo: string;
    telefono?: string | null;
    identificacion?: string | null;
  } | null;
  impuestoPorcentaje?: number | null;
  diasDeValidez?: number | null;
  notas?: string | null;
  items: ItemPayload[];
}

/** Gestión del equipo (rol ADMIN). */
@Injectable({ providedIn: 'root' })
export class CotizacionesApi {
  private readonly http = inject(HttpClient);

  listar(estado?: EstadoCotizacion) {
    return this.http.get<Cotizacion[]>('/api/cotizaciones', { params: estado ? { estado } : {} });
  }

  obtener(id: string) {
    return this.http.get<Cotizacion>(`/api/cotizaciones/${id}`);
  }

  abrir(payload: NuevoBorradorPayload) {
    return this.http.post<Cotizacion>('/api/cotizaciones', payload);
  }

  editar(id: string, items: ItemPayload[], notas: string | null) {
    return this.http.put<Cotizacion>(`/api/cotizaciones/${id}`, { items, notas });
  }

  enviar(id: string) {
    return this.http.post<Cotizacion>(`/api/cotizaciones/${id}/envio`, null);
  }

  cancelar(id: string) {
    return this.http.delete<void>(`/api/cotizaciones/${id}`);
  }

  descargar(id: string) {
    return this.http.get(`/api/cotizaciones/${id}/documento`, { responseType: 'blob' });
  }
}

/** Vista del cliente: el servidor filtra por el correo de su token. */
@Injectable({ providedIn: 'root' })
export class MisCotizacionesApi {
  private readonly http = inject(HttpClient);

  listar() {
    return this.http.get<Cotizacion[]>('/api/mis-cotizaciones');
  }

  obtener(id: string) {
    return this.http.get<Cotizacion>(`/api/mis-cotizaciones/${id}`);
  }

  aceptar(id: string) {
    return this.http.post<Cotizacion>(`/api/mis-cotizaciones/${id}/aceptacion`, null);
  }

  rechazar(id: string) {
    return this.http.post<Cotizacion>(`/api/mis-cotizaciones/${id}/rechazo`, null);
  }

  descargar(id: string) {
    return this.http.get(`/api/mis-cotizaciones/${id}/documento`, { responseType: 'blob' });
  }
}
