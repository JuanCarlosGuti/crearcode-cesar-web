/**
 * Esquema de datos de contenido editorial del sitio. Ningún componente
 * debe tener texto embebido: todo pasa por estos tipos y los archivos
 * de datos en `contenido/` (ver docs/02-arquitectura.md ADR-05).
 */

export interface PasoDeProceso {
  readonly titulo: string;
  readonly descripcion: string;
}

export interface PreguntaFrecuente {
  readonly pregunta: string;
  readonly respuesta: string;
}

export interface Servicio {
  readonly slug: string;
  readonly nombre: string;
  readonly resumenCorto: string;
  readonly problema: string;
  readonly incluye: readonly string[];
  readonly proceso: readonly PasoDeProceso[];
  readonly entregables: readonly string[];
  readonly faq: readonly PreguntaFrecuente[];
  readonly mensajeWhatsapp: string;
}

export interface Caso {
  readonly slug: string;
  readonly titulo: string;
  readonly reto: string;
  readonly solucion: string;
  readonly resultado: string;
  readonly metaDescripcion: string;
}

export interface ArticuloBlog {
  readonly slug: string;
  readonly titulo: string;
  readonly resumen: string;
  readonly fecha: string;
  readonly cuerpoMarkdown: string;
}

export interface ValorEmpresa {
  readonly titulo: string;
  readonly descripcion: string;
}

export interface DocumentoLegal {
  readonly titulo: string;
  readonly metaDescripcion: string;
  readonly parrafos: readonly string[];
  readonly notaBorrador: string;
}
