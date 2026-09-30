package com.crearcode.leads.dominio;

import java.time.LocalDate;

/**
 * Todo lo que el equipo hace sobre un proyecto desde el panel (HU-54 a
 * HU-56): el plan, los estados, los pagos y la pausa. Cada operación
 * devuelve el proyecto ya guardado.
 */
public interface GestionarProyectoUseCase {

	Proyecto cambiarDescripcion(ProyectoId id, DescripcionDelProyecto descripcion);

	Proyecto agregarFase(ProyectoId id, DatosDeFase datos);

	Proyecto editarFase(ProyectoId id, FaseId fase, DatosDeFase datos);

	Proyecto moverFase(ProyectoId id, FaseId fase, int posicion);

	Proyecto quitarFase(ProyectoId id, FaseId fase);

	Proyecto escribirResumenDeFase(ProyectoId id, FaseId fase, String resumen);

	Proyecto agregarEntregable(ProyectoId id, FaseId fase, DatosDeEntregable datos, boolean esCambioDeAlcance);

	Proyecto editarEntregable(ProyectoId id, EntregableId entregable, DatosDeEntregable datos);

	Proyecto ponerDemo(ProyectoId id, EntregableId entregable, UrlDeDemo demo);

	Proyecto moverEntregable(ProyectoId id, EntregableId entregable, FaseId destino, int posicion);

	Proyecto quitarEntregable(ProyectoId id, EntregableId entregable);

	Proyecto cambiarEstadoDeEntregable(ProyectoId id, EntregableId entregable, EstadoEntregable destino,
			String nota);

	/** Registra un pago recibido a mano (decisión 24): el origen siempre es MANUAL. */
	Proyecto registrarPago(ProyectoId id, EntregableId entregable, Dinero monto, LocalDate fecha,
			MedioDePago medio, String referencia, Correo registradoPor);

	Proyecto pausar(ProyectoId id);

	Proyecto reanudar(ProyectoId id);

	Proyecto cerrar(ProyectoId id);

}
