package com.crearcode.leads.dominio;

import java.time.Instant;
import java.util.Objects;

/**
 * Resultado de un login exitoso: el token de sesión (JWT en la
 * implementación real) y el instante en que deja de ser válido.
 */
public record SesionAutenticada(String token, Instant expiraEn) {

	public SesionAutenticada {
		Objects.requireNonNull(token, "El token de sesión no puede ser nulo");
		Objects.requireNonNull(expiraEn, "La expiración de la sesión no puede ser nula");
	}

}
