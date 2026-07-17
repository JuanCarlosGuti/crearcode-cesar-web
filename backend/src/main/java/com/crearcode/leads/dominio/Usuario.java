package com.crearcode.leads.dominio;

import java.util.Objects;

/**
 * Agregado raíz del contexto {@code usuarios}. Identidad de quien
 * accede al panel admin (ver ADR-08 en docs/02-arquitectura.md). A
 * diferencia de {@link SolicitudDeContacto}, no tiene estado mutable en
 * v1.
 */
public record Usuario(UsuarioId id, Correo correo, String contrasenaHash, Rol rol) {

	public Usuario {
		Objects.requireNonNull(id, "El id del usuario no puede ser nulo");
		Objects.requireNonNull(correo, "El correo del usuario no puede ser nulo");
		if (contrasenaHash == null || contrasenaHash.isBlank()) {
			throw new IllegalArgumentException("El hash de la contraseña no puede estar vacío");
		}
		Objects.requireNonNull(rol, "El rol del usuario no puede ser nulo");
	}

	public static Usuario crear(Correo correo, String contrasenaHash, Rol rol) {
		return new Usuario(UsuarioId.nuevo(), correo, contrasenaHash, rol);
	}

}
