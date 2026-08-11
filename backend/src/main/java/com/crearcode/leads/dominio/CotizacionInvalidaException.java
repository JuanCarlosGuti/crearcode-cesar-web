package com.crearcode.leads.dominio;

/**
 * Violación de una invariante del contexto de cotizaciones (fase F11).
 */
public class CotizacionInvalidaException extends RuntimeException {

	public CotizacionInvalidaException(String mensaje) {
		super(mensaje);
	}

}
