package com.crearcode.leads.aplicacion;

/**
 * Se lanza cuando el login es correcto pero la cuenta aún no verificó
 * su correo (invariante 4 del contexto usuarios). Solo se lanza DESPUÉS
 * de comprobar la contraseña: con la contraseña equivocada la respuesta
 * sigue siendo el genérico de {@link CredencialesInvalidasException},
 * para no revelar el estado de una cuenta ajena (invariante 6).
 */
public class CuentaNoVerificadaException extends RuntimeException {

	public CuentaNoVerificadaException() {
		super("La cuenta aún no está verificada: revisa tu correo o pide un nuevo enlace");
	}

}
