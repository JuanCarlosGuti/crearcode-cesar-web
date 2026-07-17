package com.crearcode.leads.aplicacion;

import com.crearcode.leads.dominio.Correo;

/** Se lanza al intentar crear un usuario con un correo ya registrado. */
public class UsuarioYaExisteException extends RuntimeException {

	public UsuarioYaExisteException(Correo correo) {
		super("Ya existe un usuario con el correo " + correo.valor());
	}

}
