package com.crearcode.leads.dominio;

import java.time.Instant;

/**
 * Value object que registra la aceptación (o no) del tratamiento de datos
 * personales (Ley 1581 de 2012). {@code aceptado = false} es un estado
 * representable a propósito: la invariante de que una
 * {@link SolicitudDeContacto} exige consentimiento aceptado se aplica en
 * {@code SolicitudDeContacto.registrar()}, no aquí.
 *
 * <p>
 * {@code comunicacionesComerciales} es una autorización DISTINTA, con su
 * propia casilla y opcional (política v2, sección 13). Van juntas en el
 * mismo value object porque comparten la prueba —la fecha y la versión
 * de la política que se aceptó—, pero se guardan por separado: es lo
 * único que permite retirar la comercial «sin que afecte lo demás», y
 * saber a quién se le puede escribir sin revisar correos viejos.
 */
public record ConsentimientoDatos(boolean aceptado, Instant fechaAceptacion, String versionPoliticaAceptada,
		boolean comunicacionesComerciales) {

	/** Solo el tratamiento obligatorio, sin finalidad comercial. */
	public ConsentimientoDatos(boolean aceptado, Instant fechaAceptacion, String versionPoliticaAceptada) {
		this(aceptado, fechaAceptacion, versionPoliticaAceptada, false);
	}

	public ConsentimientoDatos {
		if (fechaAceptacion == null) {
			throw new ConsentimientoRequeridoException("La fecha de aceptación es obligatoria");
		}
		if (versionPoliticaAceptada == null || versionPoliticaAceptada.isBlank()) {
			throw new ConsentimientoRequeridoException("La versión de la política aceptada es obligatoria");
		}
		if (comunicacionesComerciales && !aceptado) {
			// Si no autorizó lo básico, no hay nada que sostenga lo
			// accesorio: guardarlo sería inventar una autorización.
			throw new ConsentimientoRequeridoException(
					"No se puede autorizar el envío comercial sin autorizar el tratamiento de datos");
		}
	}

}
