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

// --- Vista del equipo (rol ADMIN, F12 entregable 3) ---

export interface ResumenDeProyecto {
  id: string;
  nombre: string;
  clienteNombre: string;
  clienteCorreo: string;
  estado: EstadoProyecto;
  avance: number;
  total: number;
  saldo: number;
  pendienteDePago: number;
  actualizadoEn: string;
}

export interface DatosDeFasePayload {
  nombre: string;
  objetivo: string | null;
  inicioPlaneado: string | null;
  finPlaneado: string | null;
}

export interface DatosDeEntregablePayload {
  nombre: string;
  descripcion: string | null;
  valor: number;
  momentoDeCobro: MomentoDeCobro;
}

export interface FaseDelPlan {
  fase: DatosDeFasePayload;
  entregables: DatosDeEntregablePayload[];
}

export interface DescripcionPayload {
  nombre: string;
  descripcion: string | null;
  inicio: string;
  entregaEstimada: string | null;
}

export interface NuevoProyectoPayload {
  cotizacionId: string | null;
  cliente: { nombre: string; correo: string } | null;
  impuestoPorcentaje: number | null;
  descripcion: DescripcionPayload;
  plan: FaseDelPlan[];
}

export interface PagoPayload {
  entregableId: string;
  monto: number;
  fecha: string;
  medio: MedioDePago;
  referencia: string | null;
}

/** Gestión del equipo (rol ADMIN). Cada cambio devuelve el proyecto ya recalculado. */
@Injectable({ providedIn: 'root' })
export class ProyectosApi {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/proyectos';

  listar() {
    return this.http.get<ResumenDeProyecto[]>(this.base);
  }

  obtener(id: string) {
    return this.http.get<Proyecto>(`${this.base}/${id}`);
  }

  deLaCotizacion(cotizacion: string) {
    return this.http.get<ResumenDeProyecto>(`${this.base}/de-cotizacion/${cotizacion}`);
  }

  proponerPlan(cotizacion: string) {
    return this.http.get<FaseDelPlan[]>(`${this.base}/propuesta`, { params: { cotizacion } });
  }

  crear(payload: NuevoProyectoPayload) {
    return this.http.post<Proyecto>(this.base, payload);
  }

  cambiarDescripcion(id: string, descripcion: DescripcionPayload) {
    return this.http.put<Proyecto>(`${this.base}/${id}/descripcion`, descripcion);
  }

  agregarFase(id: string, fase: DatosDeFasePayload) {
    return this.http.post<Proyecto>(`${this.base}/${id}/fases`, fase);
  }

  editarFase(id: string, fase: string, datos: DatosDeFasePayload) {
    return this.http.put<Proyecto>(`${this.base}/${id}/fases/${fase}`, datos);
  }

  moverFase(id: string, fase: string, posicion: number) {
    return this.http.put<Proyecto>(`${this.base}/${id}/fases/${fase}/posicion`, { posicion });
  }

  quitarFase(id: string, fase: string) {
    return this.http.delete<Proyecto>(`${this.base}/${id}/fases/${fase}`);
  }

  escribirResumen(id: string, fase: string, resumen: string) {
    return this.http.put<Proyecto>(`${this.base}/${id}/fases/${fase}/resumen`, { resumen });
  }

  agregarEntregable(id: string, fase: string, entregable: DatosDeEntregablePayload, esCambioDeAlcance: boolean) {
    return this.http.post<Proyecto>(`${this.base}/${id}/fases/${fase}/entregables`, { entregable, esCambioDeAlcance });
  }

  editarEntregable(id: string, entregable: string, datos: DatosDeEntregablePayload) {
    return this.http.put<Proyecto>(`${this.base}/${id}/entregables/${entregable}`, datos);
  }

  ponerDemo(id: string, entregable: string, url: string | null) {
    return this.http.put<Proyecto>(`${this.base}/${id}/entregables/${entregable}/demo`, { url });
  }

  moverEntregable(id: string, entregable: string, faseId: string, posicion: number) {
    return this.http.put<Proyecto>(`${this.base}/${id}/entregables/${entregable}/posicion`, { faseId, posicion });
  }

  quitarEntregable(id: string, entregable: string) {
    return this.http.delete<Proyecto>(`${this.base}/${id}/entregables/${entregable}`);
  }

  cambiarEstado(id: string, entregable: string, estado: EstadoEntregable, nota: string | null) {
    return this.http.post<Proyecto>(`${this.base}/${id}/entregables/${entregable}/estado`, { estado, nota });
  }

  registrarPago(id: string, pago: PagoPayload) {
    return this.http.post<Proyecto>(`${this.base}/${id}/pagos`, pago);
  }

  pausar(id: string) {
    return this.http.post<Proyecto>(`${this.base}/${id}/pausa`, null);
  }

  reanudar(id: string) {
    return this.http.post<Proyecto>(`${this.base}/${id}/reanudacion`, null);
  }

  cerrar(id: string) {
    return this.http.post<Proyecto>(`${this.base}/${id}/cierre`, null);
  }
}

/**
 * Las transiciones que el panel ofrece para cada estado. Es solo para no
 * mostrar botones inútiles: quien decide es el servidor (docs/03 Parte 5).
 */
export const TRANSICIONES_DEL_EQUIPO: Record<EstadoEntregable, EstadoEntregable[]> = {
  PENDIENTE: ['EN_CURSO'],
  EN_CURSO: ['EN_REVISION'],
  EN_REVISION: ['APROBADO', 'CON_AJUSTES'],
  CON_AJUSTES: ['EN_CURSO'],
  APROBADO: [],
};
