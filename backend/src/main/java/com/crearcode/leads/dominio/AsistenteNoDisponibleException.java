package com.crearcode.leads.dominio;

/**
 * El proveedor de IA falló o está saturado (timeout, 429, 5xx). La
 * capa de aplicación la captura y responde con el mensaje de
 * indisponibilidad (invariante 3 del contexto asistente).
 */
public class AsistenteNoDisponibleException extends RuntimeException {

	public AsistenteNoDisponibleException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}

	public AsistenteNoDisponibleException(String mensaje) {
		super(mensaje);
	}

}
