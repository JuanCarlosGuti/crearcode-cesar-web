/**
 * Formatos que el portal de proyectos (F12) muestra al cliente. Las
 * pantallas de F11 tienen su propio formateo inline; esto no los
 * reemplaza para no mezclar una refactorización con la fase.
 */

const PESOS = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

const FECHA_LARGA = new Intl.DateTimeFormat('es-CO', {
  day: 'numeric',
  month: 'long',
  year: 'numeric',
  timeZone: 'UTC',
});

const FECHA_LARGA_EN_COLOMBIA = new Intl.DateTimeFormat('es-CO', {
  day: 'numeric',
  month: 'long',
  year: 'numeric',
  timeZone: 'America/Bogota',
});

export function formatearPesos(valor: number): string {
  return PESOS.format(valor);
}

/**
 * Fecha sin hora ("2026-09-01"), como las fechas planeadas del proyecto.
 * Se arma y se formatea en UTC a propósito: así el día que se muestra es
 * el que dice el texto, sin importar la zona del navegador.
 */
export function formatearFecha(fecha: string | null | undefined): string {
  if (!fecha) {
    return '';
  }
  const [anio, mes, dia] = fecha.split('-').map(Number);
  return FECHA_LARGA.format(new Date(Date.UTC(anio, mes - 1, dia)));
}

/** Un instante (con hora), mostrado como el día que es en Colombia. */
export function formatearFechaDeInstante(instante: string | null | undefined): string {
  if (!instante) {
    return '';
  }
  return FECHA_LARGA_EN_COLOMBIA.format(new Date(instante));
}

const DIA_EN_COLOMBIA = new Intl.DateTimeFormat('en-CA', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  timeZone: 'America/Bogota',
});

/** El día de hoy en Colombia como "AAAA-MM-DD", el formato de los campos de fecha. */
export function hoyEnColombia(ahora: Date = new Date()): string {
  return DIA_EN_COLOMBIA.format(ahora);
}
