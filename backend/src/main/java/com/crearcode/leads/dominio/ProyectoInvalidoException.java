package com.crearcode.leads.dominio;

/**
 * Violación de una invariante del contexto de proyectos (fase F12).
 */
public class ProyectoInvalidoException extends RuntimeException {

	public ProyectoInvalidoException(String mensaje) {
		super(mensaje);
	}

}
