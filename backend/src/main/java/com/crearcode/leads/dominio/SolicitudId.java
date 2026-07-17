package com.crearcode.leads.dominio;

import java.util.Objects;
import java.util.UUID;

public record SolicitudId(UUID valor) {

	public SolicitudId {
		Objects.requireNonNull(valor, "El id de la solicitud no puede ser nulo");
	}

	public static SolicitudId nuevo() {
		return new SolicitudId(UUID.randomUUID());
	}

}
