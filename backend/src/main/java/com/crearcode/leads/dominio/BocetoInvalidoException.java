package com.crearcode.leads.dominio;

/**
 * El boceto que devolvió el proveedor de IA no cumple las reglas del
 * dominio (ISS-230). No es culpa del visitante: el adaptador la
 * convierte en una falla del proveedor, que pasa al respaldo y nunca
 * llega al navegador como un 400.
 */
public class BocetoInvalidoException extends RuntimeException {

	public BocetoInvalidoException(String mensaje) {
		super(mensaje);
	}

}
