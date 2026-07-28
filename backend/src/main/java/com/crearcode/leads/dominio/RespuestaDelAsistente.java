package com.crearcode.leads.dominio;

import java.util.Objects;

/**
 * Lo que el asistente devuelve al visitante (F9): el texto y si
 * sugirió escalar a un humano (la interfaz muestra el CTA contextual
 * de WhatsApp/contacto cuando {@code escalarAHumano} es true).
 */
public record RespuestaDelAsistente(String texto, boolean escalarAHumano) {

	public RespuestaDelAsistente {
		Objects.requireNonNull(texto, "El texto de la respuesta no puede ser nulo");
	}

}
