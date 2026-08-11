package com.crearcode.leads.aplicacion;

import com.crearcode.leads.dominio.CotizacionId;

public class CotizacionNoEncontradaException extends RuntimeException {

	public CotizacionNoEncontradaException(CotizacionId id) {
		super("No existe la cotización " + id.valor());
	}

}
