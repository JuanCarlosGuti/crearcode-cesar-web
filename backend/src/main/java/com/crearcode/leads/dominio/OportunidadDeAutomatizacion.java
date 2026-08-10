package com.crearcode.leads.dominio;

/**
 * Una oportunidad de la radiografía: título, explicación y el
 * beneficio en una frase (formato del prototipo aprobado).
 */
public record OportunidadDeAutomatizacion(String titulo, String detalle, String beneficio) {

	public OportunidadDeAutomatizacion {
		titulo = titulo == null ? "" : titulo.trim();
		detalle = detalle == null ? "" : detalle.trim();
		beneficio = beneficio == null ? "" : beneficio.trim();
		if (titulo.isEmpty() || detalle.isEmpty() || beneficio.isEmpty()) {
			throw new DiagnosticoInvalidoException("Una oportunidad requiere título, detalle y beneficio");
		}
	}

}
