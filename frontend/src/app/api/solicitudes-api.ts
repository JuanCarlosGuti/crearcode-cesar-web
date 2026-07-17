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

@Injectable({ providedIn: 'root' })
export class SolicitudesApi {
  private readonly http = inject(HttpClient);

  registrar(payload: SolicitudContactoPayload) {
    return this.http.post<SolicitudCreada>('/api/solicitudes', payload);
  }
}
