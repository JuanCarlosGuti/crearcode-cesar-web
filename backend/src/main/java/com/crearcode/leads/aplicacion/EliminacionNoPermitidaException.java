package com.crearcode.leads.aplicacion;

/**
 * Esa cuenta no se puede eliminar desde la pantalla del cliente. Hoy
 * aplica a las de administrador: el panel es de la empresa, y dejar
 * que se borre sola desde una pantalla pública dejaría el sitio sin
 * quien lo administre.
 */
public class EliminacionNoPermitidaException extends RuntimeException {

	public EliminacionNoPermitidaException() {
		super("Esta cuenta no se puede eliminar desde aquí");
	}

}
