import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export interface SolicitudContactoPayload {
  nombre: string;
  empresa: string;
  correo: string;
  telefono: string;
  servicioDeInteres: string;
  mensaje: string;
  aceptaConsentimiento: boolean;
  sitioWeb: string;
}

export interface SolicitudCreada {
  id: string;
}

export type EstadoSolicitud = 'NUEVA' | 'CONTACTADA' | 'CONVERTIDA' | 'DESCARTADA';

export interface Solicitud {
  id: string;
  nombre: string;
  empresa: string | null;
  correo: string;
  telefono: string;
  servicioDeInteres: string;
  mensaje: string;
  estado: EstadoSolicitud;
  fechaCreacion: string;
  fechaUltimaActualizacion: string;
}

@Injectable({ providedIn: 'root' })
export class SolicitudesApi {
  private readonly http = inject(HttpClient);

  registrar(payload: SolicitudContactoPayload) {
    return this.http.post<SolicitudCreada>('/api/solicitudes', payload);
  }

  listar(estado?: EstadoSolicitud) {
    return this.http.get<Solicitud[]>('/api/solicitudes', { params: estado ? { estado } : {} });
  }

  cambiarEstado(id: string, nuevoEstado: EstadoSolicitud) {
    return this.http.patch<void>(`/api/solicitudes/${id}/estado`, { nuevoEstado });
  }
}
