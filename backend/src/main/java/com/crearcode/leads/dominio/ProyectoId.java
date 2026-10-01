package com.crearcode.leads.dominio;

import java.util.Objects;
import java.util.UUID;

public record ProyectoId(UUID valor) {

	public ProyectoId {
		Objects.requireNonNull(valor, "El id del proyecto no puede ser nulo");
	}

	public static ProyectoId nuevo() {
		return new ProyectoId(UUID.randomUUID());
	}

}
