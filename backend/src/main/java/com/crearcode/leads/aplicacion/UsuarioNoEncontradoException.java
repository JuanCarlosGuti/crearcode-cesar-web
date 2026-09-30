package com.crearcode.leads.aplicacion;

/** No existe una cuenta con ese correo. */
public class UsuarioNoEncontradoException extends RuntimeException {

	public UsuarioNoEncontradoException() {
		super("No encontramos una cuenta con ese correo");
	}

}
