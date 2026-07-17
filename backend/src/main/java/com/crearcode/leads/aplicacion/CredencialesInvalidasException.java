package com.crearcode.leads.aplicacion;

/**
 * Se lanza cuando el login falla, sin distinguir si el correo no existe
 * o la contraseña no coincide (ver docs/03-modelo-de-dominio.md, Parte
 * 2, invariante 1 — evita que el mensaje permita enumerar correos
 * válidos).
 */
public class CredencialesInvalidasException extends RuntimeException {

	public CredencialesInvalidasException() {
		super("Correo o contraseña incorrectos");
	}

}
