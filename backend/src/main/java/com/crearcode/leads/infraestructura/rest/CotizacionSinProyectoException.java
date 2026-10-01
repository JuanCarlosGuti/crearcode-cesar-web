package com.crearcode.leads.infraestructura.rest;

import com.crearcode.leads.dominio.CotizacionId;

/** La cotización todavía no tiene proyecto: el panel ofrece crearlo (404). */
class CotizacionSinProyectoException extends RuntimeException {

	CotizacionSinProyectoException(CotizacionId id) {
		super("La cotización " + id.valor() + " todavía no tiene proyecto");
	}

}
