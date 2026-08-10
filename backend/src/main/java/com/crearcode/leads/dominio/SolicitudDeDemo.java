package com.crearcode.leads.dominio;

/**
 * Lo que el cliente registrado describe de su negocio para el demo de
 * diseño (F10d, HU-42). Los tres campos son DATOS del visitante: la
 * aplicación los delimita en los prompts (anti-inyección).
 */
public record SolicitudDeDemo(String sector, String queHace, String queNecesita) {

	public static final int LONGITUD_MAXIMA_SECTOR = 60;
	public static final int LONGITUD_MAXIMA_TEXTO = 300;

	public SolicitudDeDemo {
		sector = normalizar(sector, LONGITUD_MAXIMA_SECTOR, "El sector");
		queHace = normalizar(queHace, LONGITUD_MAXIMA_TEXTO, "Qué hace tu negocio");
		queNecesita = normalizar(queNecesita, LONGITUD_MAXIMA_TEXTO, "Qué necesitas resolver");
	}

	private static String normalizar(String valor, int maximo, String campo) {
		String limpio = valor == null ? "" : valor.trim();
		if (limpio.isEmpty() || limpio.length() > maximo) {
			throw new SolicitudDeDemoInvalidaException(
					"%s es obligatorio y no puede superar %d caracteres".formatted(campo, maximo));
		}
		return limpio;
	}

}
