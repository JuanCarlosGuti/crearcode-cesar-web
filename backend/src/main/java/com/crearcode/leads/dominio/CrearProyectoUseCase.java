package com.crearcode.leads.dominio;

import java.util.List;

/** Crear un proyecto (HU-53): desde una cotización aceptada o en blanco. */
public interface CrearProyectoUseCase {

	/**
	 * Propone el plan a partir de los ítems de la cotización: una fase
	 * con un entregable por ítem, el primero cobrado al iniciar y el
	 * resto al aprobar (decisión 21). Es solo una propuesta: el equipo la
	 * reorganiza antes de confirmar.
	 */
	List<NuevaFase> proponerPlan(CotizacionId cotizacion);

	Proyecto desdeCotizacion(CotizacionId cotizacion, DescripcionDelProyecto descripcion, List<NuevaFase> plan);

	Proyecto enBlanco(Correo correoDelCliente, String nombreDelCliente, DescripcionDelProyecto descripcion,
			Porcentaje impuesto, List<NuevaFase> plan);

}
