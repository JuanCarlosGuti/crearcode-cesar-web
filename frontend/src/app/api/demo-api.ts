import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export interface SolicitudDeDemo {
  sector: string;
  queHace: string;
  queNecesita: string;
}

export interface BocetoDeDemo {
  titulo: string;
  funcionalidades: string[];
  imagenBase64: string;
  tipoMime: string;
}

@Injectable({ providedIn: 'root' })
export class DemoApi {
  private readonly http = inject(HttpClient);

  /**
   * Solo registrados (HU-42): el interceptor agrega el Bearer de la
   * sesión; sin token el backend responde 401.
   */
  generar(solicitud: SolicitudDeDemo) {
    return this.http.post<BocetoDeDemo>('/api/asistente/demo-diseno', solicitud);
  }
}
