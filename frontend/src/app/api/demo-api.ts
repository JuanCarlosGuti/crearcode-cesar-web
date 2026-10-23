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
  /** Un emoji que dibuja el elemento, o vacío (decisión 35). */
  icono: string;
}

/** Decisión 35: la IA elige de estas listas cerradas y el sitio dibuja. */
export type PaletaDeBoceto =
  | 'FRESCA'
  | 'CALIDA'
  | 'NOCTURNA'
  | 'SALUD'
  | 'ELEGANTE'
  | 'INDUSTRIAL'
  | 'NATURAL'
  | 'OCEANO';

export type FormaDeLogo = 'CIRCULO' | 'CUADRADO' | 'ESCUDO' | 'HEXAGONO' | 'SELLO';

export type EstiloDeLetra = 'MODERNA' | 'CLASICA' | 'SENCILLA';

export interface LogoDeMarca {
  forma: FormaDeLogo;
  /** Un emoji, o vacío para usar la inicial del nombre. */
  simbolo: string;
  letra: EstiloDeLetra;
}

export interface IdentidadDeMarca {
  nombre: string;
  eslogan: string;
  paleta: PaletaDeBoceto;
  logo: LogoDeMarca;
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
  identidad: IdentidadDeMarca;
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
