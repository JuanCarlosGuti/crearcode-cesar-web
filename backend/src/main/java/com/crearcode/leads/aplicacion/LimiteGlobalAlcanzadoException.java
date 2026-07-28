package com.crearcode.leads.aplicacion;

/**
 * Se agotó el cupo global diario del asistente (el techo de la capa
 * gratis del proveedor, HU-38): para el visitante equivale a
 * indisponibilidad, con la alternativa humana.
 */
public class LimiteGlobalAlcanzadoException extends RuntimeException {

	public LimiteGlobalAlcanzadoException() {
		super("El asistente agotó su cupo global de hoy");
	}

}
