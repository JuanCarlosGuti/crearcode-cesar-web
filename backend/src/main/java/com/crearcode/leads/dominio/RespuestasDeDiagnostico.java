package com.crearcode.leads.dominio;

import java.util.List;

/**
 * Cuestionario respondido del diagnóstico digital (F10c, HU-41).
 */
public record RespuestasDeDiagnostico(List<ParDeDiagnostico> pares) {

	public static final int MAXIMO_DE_PARES = 10;

	public RespuestasDeDiagnostico {
		pares = List.copyOf(pares);
		if (pares.isEmpty() || pares.size() > MAXIMO_DE_PARES) {
			throw new DiagnosticoInvalidoException(
					"El cuestionario debe tener entre 1 y %d respuestas".formatted(MAXIMO_DE_PARES));
		}
	}

}
