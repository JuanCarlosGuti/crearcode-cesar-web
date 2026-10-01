import { Entregable, Fase, Proyecto } from '../../api/proyectos-api';

/** Datos de prueba del portal: los montos los "calcula el servidor" aquí a mano. */
export function entregableDePrueba(parcial: Partial<Entregable> = {}): Entregable {
  return {
    id: 'e-diseno',
    nombre: 'Diseño',
    descripcion: 'Logo y colores',
    valor: 1000000,
    impuesto: 190000,
    cobro: 1190000,
    momentoDeCobro: 'AL_INICIAR',
    esCobrable: true,
    pagado: 0,
    pendiente: 1190000,
    esCambioDeAlcance: false,
    estado: 'PENDIENTE',
    urlDemo: null,
    notaDeAjustes: null,
    aprobadoEn: null,
    aprobadoPor: null,
    ...parcial,
  };
}

export function faseDePrueba(parcial: Partial<Fase> = {}): Fase {
  return {
    id: 'f-1',
    nombre: 'Diseño de la tienda',
    objetivo: 'Que veas cómo se verá',
    inicioPlaneado: '2026-09-01',
    finPlaneado: '2026-09-15',
    resumenParaElCliente: null,
    entregables: [entregableDePrueba()],
    ...parcial,
  };
}

export function proyectoDePrueba(parcial: Partial<Proyecto> = {}): Proyecto {
  return {
    id: 'p-1',
    nombre: 'Tienda de Café Valle',
    descripcion: 'Tu tienda para vender café',
    inicio: '2026-09-01',
    entregaEstimada: '2026-12-15',
    clienteNombre: 'Café Valle',
    clienteCorreo: 'cliente@ejemplo.co',
    origenCotizacionId: null,
    impuestoPorcentaje: 19,
    estado: 'ACTIVO',
    finDeGarantia: null,
    avance: 25,
    total: 4760000,
    totalPagado: 0,
    saldo: 4760000,
    pendienteDePago: 1190000,
    creadoEn: '2026-09-01T15:00:00Z',
    actualizadoEn: '2026-09-01T15:00:00Z',
    fases: [faseDePrueba()],
    pagos: [],
    clienteTieneCuenta: null,
    ...parcial,
  };
}
