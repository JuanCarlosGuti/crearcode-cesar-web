package com.crearcode.leads.dominio;

/**
 * Se intentó responder una cotización cuya validez ya pasó. Es distinta
 * de una transición inválida: el estado era correcto, lo que falló fue
 * el plazo (invariante 4 del contexto).
 */
public class CotizacionVencidaException extends RuntimeException {

	public CotizacionVencidaException(String mensaje) {
		super(mensaje);
	}

}
