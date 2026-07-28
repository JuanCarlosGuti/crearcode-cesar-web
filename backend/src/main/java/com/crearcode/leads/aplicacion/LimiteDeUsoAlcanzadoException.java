package com.crearcode.leads.aplicacion;

/**
 * El visitante agotó su cupo diario del asistente (HU-38). La interfaz
 * distingue si era anónimo (invita a registrarse) o registrado.
 */
public class LimiteDeUsoAlcanzadoException extends RuntimeException {

	private final boolean identidadRegistrada;

	public LimiteDeUsoAlcanzadoException(boolean identidadRegistrada) {
		super("Alcanzaste tus consultas del día");
		this.identidadRegistrada = identidadRegistrada;
	}

	public boolean identidadRegistrada() {
		return identidadRegistrada;
	}

}
