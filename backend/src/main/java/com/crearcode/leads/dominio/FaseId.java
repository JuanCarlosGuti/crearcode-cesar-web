package com.crearcode.leads.dominio;

import java.util.Objects;
import java.util.UUID;

public record FaseId(UUID valor) {

	public FaseId {
		Objects.requireNonNull(valor, "El id de la fase no puede ser nulo");
	}

	public static FaseId nuevo() {
		return new FaseId(UUID.randomUUID());
	}

}
