package com.crearcode.leads.dominio;

/**
 * VO del simulador de chatbot (F10b, HU-40): el negocio que el
 * visitante describe. Sus campos son DATOS que se injertan en la
 * plantilla del prompt — la capa de aplicación los delimita para que
 * nunca actúen como instrucciones (anti-inyección).
 */
public record NegocioSimulado(String nombre, String rubro) {

	public static final int LONGITUD_MAXIMA_NOMBRE = 60;
	public static final int LONGITUD_MAXIMA_RUBRO = 40;

	public NegocioSimulado {
		nombre = nombre == null ? "" : nombre.trim();
		rubro = rubro == null ? "" : rubro.trim();
		if (nombre.isEmpty() || nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
			throw new NegocioSimuladoInvalidoException(
					"El nombre del negocio es obligatorio y no puede superar %d caracteres"
							.formatted(LONGITUD_MAXIMA_NOMBRE));
		}
		if (rubro.isEmpty() || rubro.length() > LONGITUD_MAXIMA_RUBRO) {
			throw new NegocioSimuladoInvalidoException(
					"El rubro es obligatorio y no puede superar %d caracteres"
							.formatted(LONGITUD_MAXIMA_RUBRO));
		}
	}

}
