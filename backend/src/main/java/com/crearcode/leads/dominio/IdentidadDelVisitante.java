package com.crearcode.leads.dominio;

/**
 * Quién está hablando con una herramienta de IA (F9/F10), para efectos
 * de límites de uso: un cliente registrado (clave = su correo) o una
 * sesión anónima (clave = id generado por el navegador).
 *
 * <p>
 * La clave anónima la controla el cliente, así que por sí sola no
 * limita nada: borrar {@code sessionStorage} devolvía el cupo entero,
 * cuantas veces se quisiera (auditoría del 28 sep 2026). Por eso la
 * identidad anónima trae además una {@code huellaDeRed} —el resumen
 * con sal del día de la IP, nunca la IP— contra la que se cuenta un
 * techo aparte, más alto.
 *
 * <p>
 * Son dos cuentas y no una a propósito: contar solo por red castigaría
 * a una oficina o a una red móvil con NAT, donde decenas de personas
 * comparten IP; contar solo por sesión no cuenta nada. Con las dos,
 * cada visitante tiene su cupo y la red tiene su techo.
 */
public record IdentidadDelVisitante(String clave, boolean registrada, String huellaDeRed) {

	private static final String SIN_SESION = "anonimo-sin-sesion";

	public static IdentidadDelVisitante registrada(String correo) {
		// Quien tiene cuenta ya está identificado: el techo por red no le
		// aplica, y su propia clave hace de huella.
		return new IdentidadDelVisitante(correo, true, correo);
	}

	/**
	 * Sesión anónima con la red conocida: es la forma que usa la API.
	 * Sin id de sesión, todas las del mismo día comparten un cupo
	 * anónimo.
	 */
	public static IdentidadDelVisitante anonima(String idDeSesion, String huellaDeRed) {
		String clave = idDeSesion == null || idDeSesion.isBlank() ? SIN_SESION : idDeSesion.trim();
		return new IdentidadDelVisitante(clave, false, huellaDeRed);
	}

	/**
	 * Sesión anónima sin red conocida: la sesión hace de red, así que el
	 * techo por red no se comparte con nadie. Es lo que usan las pruebas
	 * que no están ejercitando ese techo.
	 */
	public static IdentidadDelVisitante anonima(String idDeSesion) {
		IdentidadDelVisitante identidad = anonima(idDeSesion, null);
		return new IdentidadDelVisitante(identidad.clave(), false, identidad.clave());
	}

}
