package com.crearcode.leads.dominio;

import java.util.Objects;
import java.util.UUID;

public record CotizacionId(UUID valor) {

	public CotizacionId {
		Objects.requireNonNull(valor, "El id de la cotización no puede ser nulo");
	}

	public static CotizacionId nuevo() {
		return new CotizacionId(UUID.randomUUID());
	}

}
