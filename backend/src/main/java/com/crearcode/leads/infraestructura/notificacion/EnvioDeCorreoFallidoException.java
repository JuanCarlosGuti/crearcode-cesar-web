package com.crearcode.leads.infraestructura.notificacion;

/**
 * El correo no salió. Todos los envíos del sitio son best-effort, así
 * que quien la recibe la registra y sigue: ningún flujo de negocio se
 * cae porque el correo falle.
 */
class EnvioDeCorreoFallidoException extends RuntimeException {

	EnvioDeCorreoFallidoException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}

}
