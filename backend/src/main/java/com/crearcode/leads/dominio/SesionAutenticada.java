package com.crearcode.leads.dominio;

import java.time.Instant;
import java.util.Objects;

/**
 * Resultado de un login exitoso: el token de sesión (JWT en la
 * implementación real), su expiración, y el rol y correo del usuario —
 * el frontend los necesita para decidir a dónde llevarlo y qué mostrar
 * en "Mi cuenta" (fase F8, HU-33) sin decodificar el JWT.
 */
public record SesionAutenticada(String token, Instant expiraEn, Rol rol, Correo correo) {

	public SesionAutenticada {
		Objects.requireNonNull(token, "El token de sesión no puede ser nulo");
		Objects.requireNonNull(expiraEn, "La expiración de la sesión no puede ser nula");
		Objects.requireNonNull(rol, "El rol de la sesión no puede ser nulo");
		Objects.requireNonNull(correo, "El correo de la sesión no puede ser nulo");
	}

}
