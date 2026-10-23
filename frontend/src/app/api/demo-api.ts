import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export interface SolicitudDeDemo {
  sector: string;
  queHace: string;
  queNecesita: string;
}

/** El catálogo cerrado de piezas del boceto (ADR-16); lo valida el backend. */
export type TipoDePieza =
  | 'ENCABEZADO'
  | 'BANNER'
  | 'BUSCADOR'
  | 'CATEGORIAS'
  | 'TARJETAS'
  | 'LISTA'
  | 'FORMULARIO'
  | 'BOTON'
  | 'INDICADORES'
  | 'CHAT'
  | 'HORARIOS'
  | 'NAVEGACION';

export interface ElementoDePieza {
  texto: string;
  detalle: string;
}

export interface PiezaDeBoceto {
  tipo: TipoDePieza;
  titulo: string;
  elementos: ElementoDePieza[];
}

export interface PantallaDeBoceto {
  nombre: string;
  piezas: PiezaDeBoceto[];
}

/**
 * Desde ISS-233 el boceto son datos que el sitio dibuja, y la imagen de
 * referencia es opcional: llega en null si está apagada o su proveedor
 * falló.
 */
export interface BocetoDeDemo {
  titulo: string;
  funcionalidades: string[];
  pantallas: PantallaDeBoceto[];
  imagenBase64: string | null;
  tipoMime: string | null;
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
