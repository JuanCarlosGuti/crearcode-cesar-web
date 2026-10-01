import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export type EstadoProyecto = 'ACTIVO' | 'PAUSADO' | 'EN_GARANTIA' | 'CERRADO';
export type EstadoEntregable = 'PENDIENTE' | 'EN_CURSO' | 'EN_REVISION' | 'CON_AJUSTES' | 'APROBADO';
export type MomentoDeCobro = 'AL_INICIAR' | 'AL_APROBAR';
export type MedioDePago = 'TRANSFERENCIA' | 'NEQUI_DAVIPLATA' | 'EFECTIVO' | 'LINK_DE_PAGO';
export type OrigenDePago = 'MANUAL' | 'PASARELA';
export type QuienResponde = 'CLIENTE' | 'EQUIPO';

/**
 * Todo el dinero llega calculado por el servidor (ADR-15): el frontend
 * lo muestra y nunca lo calcula.
 */
export interface Entregable {
  id: string;
  nombre: string;
  descripcion: string | null;
  valor: number;
  impuesto: number;
  /** Lo que el cliente paga: valor más impuesto. */
  cobro: number;
  momentoDeCobro: MomentoDeCobro;
  esCobrable: boolean;
  pagado: number;
  pendiente: number;
  esCambioDeAlcance: boolean;
  estado: EstadoEntregable;
  urlDemo: string | null;
  notaDeAjustes: string | null;
  aprobadoEn: string | null;
  aprobadoPor: QuienResponde | null;
}

export interface Fase {
  id: string;
  nombre: string;
  objetivo: string | null;
  /** Fechas sin hora: "AAAA-MM-DD". */
  inicioPlaneado: string | null;
  finPlaneado: string | null;
  resumenParaElCliente: string | null;
  entregables: Entregable[];
}

export interface Pago {
  id: string;
  entregableId: string;
  monto: number;
  fecha: string;
  medio: MedioDePago;
  origen: OrigenDePago;
  referencia: string | null;
}

export interface Proyecto {
  id: string;
  nombre: string;
  descripcion: string | null;
  inicio: string;
  entregaEstimada: string | null;
  clienteNombre: string;
  clienteCorreo: string;
  origenCotizacionId: string | null;
  impuestoPorcentaje: number;
  estado: EstadoProyecto;
  /** Instante (con hora). */
  finDeGarantia: string | null;
  avance: number;
  total: number;
  totalPagado: number;
  saldo: number;
  pendienteDePago: number;
  creadoEn: string;
  actualizadoEn: string;
  fases: Fase[];
  pagos: Pago[];
  /** Solo en la vista del equipo; nulo en la del cliente. */
  clienteTieneCuenta: boolean | null;
}

/**
 * La vista del cliente (HU-49 a HU-51, HU-57). El backend filtra por el
 * correo del token: aquí no se manda ninguna identidad.
 */
@Injectable({ providedIn: 'root' })
export class MisProyectosApi {
  private readonly http = inject(HttpClient);

  listar() {
    return this.http.get<Proyecto[]>('/api/mis-proyectos');
  }

  obtener(id: string) {
    return this.http.get<Proyecto>(`/api/mis-proyectos/${id}`);
  }

  aprobar(proyecto: string, entregable: string) {
    return this.http.post<Proyecto>(`/api/mis-proyectos/${proyecto}/entregables/${entregable}/aprobacion`, null);
  }

  pedirAjustes(proyecto: string, entregable: string, nota: string) {
    return this.http.post<Proyecto>(`/api/mis-proyectos/${proyecto}/entregables/${entregable}/ajustes`, { nota });
  }
}

/** Entregables de un proyecto, en el orden del plan. */
export function entregablesDe(proyecto: Proyecto): Entregable[] {
  return proyecto.fases.flatMap((fase) => fase.entregables);
}
