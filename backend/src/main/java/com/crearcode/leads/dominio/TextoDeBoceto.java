package com.crearcode.leads.dominio;

import java.util.regex.Pattern;

/**
 * Las reglas de texto comunes a todo el boceto (ISS-230): se recortan
 * los espacios, nada pasa de su largo —se rechaza, no se corta, para no
 * dejar palabras partidas— y ningún texto muestra dinero. Es la misma
 * regla del sitio que sacó los precios de las imágenes del demo: ninguna
 * cifra que no podamos sustentar.
 */
final class TextoDeBoceto {

	/** "$", "COP" o "USD". No "peso", que también es lo que pesa una carga. */
	private static final Pattern DINERO = Pattern.compile("\\$|\\bCOP\\b|\\bUSD\\b", Pattern.CASE_INSENSITIVE);

	private TextoDeBoceto() {
	}

	static String obligatorio(String texto, int maximo, String campo) {
		String limpio = opcional(texto, maximo, campo);
		if (limpio.isEmpty()) {
			throw new BocetoInvalidoException(campo + " no puede estar vacío");
		}
		return limpio;
	}

	static String opcional(String texto, int maximo, String campo) {
		String limpio = texto == null ? "" : texto.trim();
		if (limpio.length() > maximo) {
			throw new BocetoInvalidoException("%s pasa de %d caracteres".formatted(campo, maximo));
		}
		if (DINERO.matcher(limpio).find()) {
			throw new BocetoInvalidoException(campo + " muestra dinero");
		}
		return limpio;
	}

}
