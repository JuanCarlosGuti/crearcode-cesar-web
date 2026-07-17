package com.crearcode.leads.dominio;

import java.util.Objects;
import java.util.UUID;

public record UsuarioId(UUID valor) {

	public UsuarioId {
		Objects.requireNonNull(valor, "El id del usuario no puede ser nulo");
	}

	public static UsuarioId nuevo() {
		return new UsuarioId(UUID.randomUUID());
	}

}
