package com.crearcode.leads.dominio;

/**
 * Un {@link Porcentaje} fuera de 0-100. Mismo motivo que
 * {@link MontoInvalidoException}: el porcentaje de impuesto lo usan
 * cotizaciones y proyectos.
 */
public class PorcentajeInvalidoException extends RuntimeException {

	public PorcentajeInvalidoException(String mensaje) {
		super(mensaje);
	}

}
