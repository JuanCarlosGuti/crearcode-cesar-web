package com.crearcode.leads.dominio;

/**
 * El token de correo no sirve — vencido, ya usado o inexistente. A
 * propósito es un único mensaje sin distinguir la causa (decisión 5 de
 * la fase F8): no darle un oráculo a quien pruebe tokens al azar.
 */
public class TokenDeCuentaInvalidoException extends RuntimeException {

	public TokenDeCuentaInvalidoException() {
		super("El enlace es inválido o ya venció");
	}

}
