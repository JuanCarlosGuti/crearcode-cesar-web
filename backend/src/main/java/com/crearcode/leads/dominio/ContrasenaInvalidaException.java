package com.crearcode.leads.dominio;

/**
 * La contraseña propuesta no cumple la política mínima (ver
 * {@link ContrasenaPlana}). Mensaje pensado para mostrarse tal cual al
 * usuario final.
 */
public class ContrasenaInvalidaException extends RuntimeException {

	public ContrasenaInvalidaException() {
		super("La contraseña debe tener al menos " + ContrasenaPlana.LONGITUD_MINIMA + " caracteres");
	}

}
