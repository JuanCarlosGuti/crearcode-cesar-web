package com.crearcode.leads.dominio;

/**
 * Un par pregunta→respuesta del cuestionario del diagnóstico digital
 * (F10c, HU-41). Ambos textos son DATOS del visitante: la capa de
 * aplicación los delimita en el prompt para que nunca actúen como
 * instrucciones.
 */
public record ParDeDiagnostico(String pregunta, String respuesta) {

	public static final int LONGITUD_MAXIMA_PREGUNTA = 200;
	public static final int LONGITUD_MAXIMA_RESPUESTA = 120;

	public ParDeDiagnostico {
		pregunta = pregunta == null ? "" : pregunta.trim();
		respuesta = respuesta == null ? "" : respuesta.trim();
		if (pregunta.isEmpty() || pregunta.length() > LONGITUD_MAXIMA_PREGUNTA) {
			throw new DiagnosticoInvalidoException(
					"Cada pregunta es obligatoria y no puede superar %d caracteres"
							.formatted(LONGITUD_MAXIMA_PREGUNTA));
		}
		if (respuesta.isEmpty() || respuesta.length() > LONGITUD_MAXIMA_RESPUESTA) {
			throw new DiagnosticoInvalidoException(
					"Cada respuesta es obligatoria y no puede superar %d caracteres"
							.formatted(LONGITUD_MAXIMA_RESPUESTA));
		}
	}

}
