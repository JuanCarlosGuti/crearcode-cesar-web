package com.crearcode.leads.dominio;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** Las formas con las que el sitio dibuja el logo (decisión 35). */
public enum FormaDeLogo {

	CIRCULO,
	CUADRADO,
	ESCUDO,
	HEXAGONO,
	SELLO;

	/** Sin importar mayúsculas: la API no las garantiza en un enum. */
	public static Optional<FormaDeLogo> desde(String nombre) {
		if (nombre == null) {
			return Optional.empty();
		}
		String buscado = nombre.strip().toUpperCase(Locale.ROOT);
		return Arrays.stream(values()).filter(forma -> forma.name().equals(buscado)).findFirst();
	}

}
