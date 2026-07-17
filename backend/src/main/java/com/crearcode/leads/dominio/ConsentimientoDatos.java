package com.crearcode.leads.dominio;

import java.time.Instant;

/**
 * Value object que registra la aceptación (o no) del tratamiento de datos
 * personales (Ley 1581 de 2012). {@code aceptado = false} es un estado
 * representable a propósito: la invariante de que una
 * {@link SolicitudDeContacto} exige consentimiento aceptado se aplica en
 * {@code SolicitudDeContacto.registrar()}, no aquí.
 */
public record ConsentimientoDatos(boolean aceptado, Instant fechaAceptacion, String versionPoliticaAceptada) {

	public ConsentimientoDatos {
		if (fechaAceptacion == null) {
			throw new ConsentimientoRequeridoException("La fecha de aceptación es obligatoria");
		}
		if (versionPoliticaAceptada == null || versionPoliticaAceptada.isBlank()) {
			throw new ConsentimientoRequeridoException("La versión de la política aceptada es obligatoria");
		}
	}

}
