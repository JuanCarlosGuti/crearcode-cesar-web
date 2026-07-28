package com.crearcode.leads.dominio;

/**
 * Quién está hablando con el asistente (F9), para efectos de límites
 * de uso: un cliente registrado (clave = su correo) o una sesión
 * anónima (clave = id generado por el navegador; blando a propósito —
 * el techo real es el límite global diario, ver ADR-10).
 */
public record IdentidadDelVisitante(String clave, boolean registrada) {

	private static final String SIN_SESION = "anonimo-sin-sesion";

	public static IdentidadDelVisitante registrada(String correo) {
		return new IdentidadDelVisitante(correo, true);
	}

	/** Sin id de sesión, todos comparten un mismo cupo anónimo. */
	public static IdentidadDelVisitante anonima(String idDeSesion) {
		String clave = idDeSesion == null || idDeSesion.isBlank() ? SIN_SESION : idDeSesion.trim();
		return new IdentidadDelVisitante(clave, false);
	}

}
