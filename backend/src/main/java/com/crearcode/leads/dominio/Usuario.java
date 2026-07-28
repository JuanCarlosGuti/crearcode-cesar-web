package com.crearcode.leads.dominio;

import java.util.Objects;

/**
 * Agregado raíz del contexto {@code usuarios}. Identidad de quien
 * accede a áreas autenticadas: el admin del panel (ADR-08, fase F5) y,
 * desde la fase F8, los clientes registrados desde el sitio público.
 * Record inmutable: los cambios de estado ({@link #verificar()},
 * {@link #conContrasena(String)}) devuelven copias.
 */
public record Usuario(UsuarioId id, Correo correo, String contrasenaHash, Rol rol, boolean verificado) {

	public Usuario {
		Objects.requireNonNull(id, "El id del usuario no puede ser nulo");
		Objects.requireNonNull(correo, "El correo del usuario no puede ser nulo");
		if (contrasenaHash == null || contrasenaHash.isBlank()) {
			throw new IllegalArgumentException("El hash de la contraseña no puede estar vacío");
		}
		Objects.requireNonNull(rol, "El rol del usuario no puede ser nulo");
	}

	/**
	 * Alta administrativa (hoy: el sembrador del admin). Nace verificado
	 * porque no pasa por el flujo de verificación por correo.
	 */
	public static Usuario crear(Correo correo, String contrasenaHash, Rol rol) {
		return new Usuario(UsuarioId.nuevo(), correo, contrasenaHash, rol, true);
	}

	/**
	 * Registro público (HU-30): nace como CLIENTE sin verificar — no
	 * puede iniciar sesión hasta consumir el token de verificación
	 * (invariante 4 del contexto).
	 */
	public static Usuario registrarCliente(Correo correo, String contrasenaHash) {
		return new Usuario(UsuarioId.nuevo(), correo, contrasenaHash, Rol.CLIENTE, false);
	}

	public Usuario verificar() {
		return new Usuario(id, correo, contrasenaHash, rol, true);
	}

	public Usuario conContrasena(String nuevoContrasenaHash) {
		return new Usuario(id, correo, nuevoContrasenaHash, rol, verificado);
	}

}
