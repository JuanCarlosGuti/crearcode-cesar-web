package com.crearcode.leads.dominio;

/**
 * Contraseña en claro <em>solo en tránsito</em> (registro y
 * restablecimiento, fase F8): nunca se persiste — se convierte en hash
 * vía {@link CifradorDeContrasenas} antes de tocar el repositorio. El
 * login y el sembrador del admin no la usan a propósito, para no
 * invalidar credenciales existentes (ver docs/03-modelo-de-dominio.md
 * Parte 2 §3).
 */
public record ContrasenaPlana(String valor) {

	public static final int LONGITUD_MINIMA = 10;

	public ContrasenaPlana {
		if (valor == null || valor.isBlank() || valor.length() < LONGITUD_MINIMA) {
			throw new ContrasenaInvalidaException();
		}
	}

	/**
	 * Enmascarado para que un log accidental jamás revele la contraseña
	 * (invariante 3 del contexto usuarios).
	 */
	@Override
	public String toString() {
		return "ContrasenaPlana[****]";
	}

}
