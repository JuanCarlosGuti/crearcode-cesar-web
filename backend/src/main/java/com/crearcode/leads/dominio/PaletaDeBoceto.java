package com.crearcode.leads.dominio;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Las ocho paletas entre las que la IA elige los colores de la app del
 * visitante (decisión 35). Los colores los define y los prueba el sitio
 * —contraste AA en cada par de texto—; el dominio solo sabe sus nombres.
 */
public enum PaletaDeBoceto {

	/** Servicios y tecnología: el verde menta de la casa. */
	FRESCA,
	/** Comida y restaurantes. */
	CALIDA,
	/** Barberías, bares y vida nocturna. */
	NOCTURNA,
	/** Salud y consultorios. */
	SALUD,
	/** Ropa, belleza y regalos. */
	ELEGANTE,
	/** Ferreterías, construcción y talleres. */
	INDUSTRIAL,
	/** Agro, café y productos naturales. */
	NATURAL,
	/** Turismo, transporte y envíos. */
	OCEANO;

	/** Sin importar mayúsculas: la API no las garantiza en un enum. */
	public static Optional<PaletaDeBoceto> desde(String nombre) {
		if (nombre == null) {
			return Optional.empty();
		}
		String buscado = nombre.strip().toUpperCase(Locale.ROOT);
		return Arrays.stream(values()).filter(paleta -> paleta.name().equals(buscado)).findFirst();
	}

}
