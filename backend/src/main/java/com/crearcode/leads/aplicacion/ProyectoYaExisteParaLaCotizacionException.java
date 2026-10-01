package com.crearcode.leads.aplicacion;

import com.crearcode.leads.dominio.CotizacionId;

/** Invariante 6: a lo sumo un proyecto por cotización. */
public class ProyectoYaExisteParaLaCotizacionException extends RuntimeException {

	public ProyectoYaExisteParaLaCotizacionException(CotizacionId id) {
		super("La cotización " + id.valor() + " ya tiene un proyecto");
	}

}
