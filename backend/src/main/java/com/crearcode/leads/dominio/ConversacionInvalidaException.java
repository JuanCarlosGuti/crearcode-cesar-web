package com.crearcode.leads.dominio;

/** La conversación del asistente no cumple los invariantes (F9). */
public class ConversacionInvalidaException extends RuntimeException {

	public ConversacionInvalidaException(String mensaje) {
		super(mensaje);
	}

}
