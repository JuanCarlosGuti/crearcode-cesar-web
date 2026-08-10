package com.crearcode.leads.dominio;

import java.util.List;

/**
 * La radiografía digital (F10c, HU-41): veredicto en una frase y
 * exactamente tres oportunidades, "ordenadas por lo rápido que verías
 * el cambio" (prototipo aprobado). Se muestra EN PANTALLA — el envío
 * por correo llega con el MVP (decisión 13 de docs/10).
 */
public record InformeDeDiagnostico(String veredicto, List<OportunidadDeAutomatizacion> oportunidades) {

	public static final int OPORTUNIDADES_REQUERIDAS = 3;

	public InformeDeDiagnostico {
		veredicto = veredicto == null ? "" : veredicto.trim();
		oportunidades = List.copyOf(oportunidades);
		if (veredicto.isEmpty()) {
			throw new DiagnosticoInvalidoException("El informe requiere un veredicto");
		}
		if (oportunidades.size() != OPORTUNIDADES_REQUERIDAS) {
			throw new DiagnosticoInvalidoException(
					"El informe requiere exactamente %d oportunidades".formatted(OPORTUNIDADES_REQUERIDAS));
		}
	}

}
