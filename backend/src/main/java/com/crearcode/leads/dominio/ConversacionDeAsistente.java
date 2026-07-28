package com.crearcode.leads.dominio;

import java.util.List;

/**
 * Historial acotado de una conversación con el asistente (F9). Sin
 * persistencia en v1: vive en la petición. El último mensaje debe ser
 * del USUARIO — es la pregunta que el asistente va a responder.
 */
public record ConversacionDeAsistente(List<MensajeDeChat> mensajes) {

	public static final int MAXIMO_DE_MENSAJES = 20;

	public ConversacionDeAsistente {
		if (mensajes == null || mensajes.isEmpty()) {
			throw new ConversacionInvalidaException("La conversación no puede estar vacía");
		}
		if (mensajes.size() > MAXIMO_DE_MENSAJES) {
			throw new ConversacionInvalidaException(
					"La conversación no puede superar los " + MAXIMO_DE_MENSAJES + " mensajes");
		}
		mensajes = List.copyOf(mensajes);
		if (mensajes.getLast().rol() != RolDeMensaje.USUARIO) {
			throw new ConversacionInvalidaException("El último mensaje debe ser del usuario");
		}
	}

	public MensajeDeChat ultimoMensaje() {
		return mensajes.getLast();
	}

}
