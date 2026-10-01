package com.crearcode.leads.dominio;

import java.util.Objects;
import java.util.UUID;

public record EntregableId(UUID valor) {

	public EntregableId {
		Objects.requireNonNull(valor, "El id del entregable no puede ser nulo");
	}

	public static EntregableId nuevo() {
		return new EntregableId(UUID.randomUUID());
	}

}
