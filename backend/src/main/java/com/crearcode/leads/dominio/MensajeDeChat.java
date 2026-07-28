package com.crearcode.leads.dominio;

import java.util.Objects;

/**
 * Un turno de la conversación con el asistente (F9). El texto se
 * normaliza (trim) y tiene longitud máxima: protege el costo por
 * tokens del proveedor y evita abusos (invariante del contexto
 * asistente, ver docs/03 Parte 3).
 */
public record MensajeDeChat(RolDeMensaje rol, String texto) {

	public static final int LONGITUD_MAXIMA = 1000;

	public MensajeDeChat {
		Objects.requireNonNull(rol, "El rol del mensaje no puede ser nulo");
		if (texto == null || texto.isBlank()) {
			throw new MensajeDeChatInvalidoException("El mensaje no puede estar vacío");
		}
		texto = texto.trim();
		if (texto.length() > LONGITUD_MAXIMA) {
			throw new MensajeDeChatInvalidoException(
					"El mensaje no puede superar los " + LONGITUD_MAXIMA + " caracteres");
		}
	}

}
