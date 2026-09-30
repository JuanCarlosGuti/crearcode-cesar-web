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

/**
 * Un proyecto del portafolio. Se llamaba Caso cuando la seccion era
 * "casos de exito"; lo que se muestra son emprendimientos propios y
 * codigo abierto, no encargos de clientes (ver docs/08 §Proyectos).
 *
 * `enlace` no es opcional a proposito: un proyecto que el visitante no
 * puede abrir es una afirmacion, y afirmaciones sin respaldo son justo
 * lo que esta seccion existe para no tener.
 */
export interface Caso {
  readonly slug: string;
  readonly titulo: string;
  /** La linea de negocio que ilustra, tal como se llama en servicios. */
  readonly linea: string;
  readonly reto: string;
  readonly solucion: string;
  readonly resultado: string;
  readonly stack: string;
  readonly enlace: { readonly url: string; readonly etiqueta: string };
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

export interface SeccionLegal {
  readonly titulo: string;
  readonly parrafos: readonly string[];
}

export interface DocumentoLegal {
  readonly titulo: string;
  readonly metaDescripcion: string;
  /**
   * Version del documento, visible al pie: la politica promete que la
   * fecha de la version vigente se indica ahi (auditoria P0-3).
   */
  readonly version: string;
  /**
   * Desde cuando rige esta version. Va en cada documento y no en una
   * constante compartida: al cambiar uno, el otro no debe heredar una
   * fecha que no es la suya.
   */
  readonly vigenteDesde: string;
  readonly secciones: readonly SeccionLegal[];
}
