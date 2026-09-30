package com.crearcode.leads.dominio;

import java.util.Objects;
import java.util.UUID;

public record PagoId(UUID valor) {

	public PagoId {
		Objects.requireNonNull(valor, "El id del pago no puede ser nulo");
	}

	public static PagoId nuevo() {
		return new PagoId(UUID.randomUUID());
	}

}
