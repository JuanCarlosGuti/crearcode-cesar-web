package com.crearcode.leads.dominio;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * El estilo de letra de la marca (decisión 35). El sitio lo traduce a
 * fuentes que ya carga o del sistema: ninguna fuente nueva que pese.
 */
public enum EstiloDeLetra {

	MODERNA,
	CLASICA,
	SENCILLA;

	/** Sin importar mayúsculas: la API no las garantiza en un enum. */
	public static Optional<EstiloDeLetra> desde(String nombre) {
		if (nombre == null) {
			return Optional.empty();
		}
		String buscado = nombre.strip().toUpperCase(Locale.ROOT);
		return Arrays.stream(values()).filter(estilo -> estilo.name().equals(buscado)).findFirst();
	}

}
