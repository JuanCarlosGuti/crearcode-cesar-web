package com.crearcode.leads.dominio;

/** El texto de un mensaje del chat no cumple los invariantes (F9). */
public class MensajeDeChatInvalidoException extends RuntimeException {

	public MensajeDeChatInvalidoException(String mensaje) {
		super(mensaje);
	}

}
