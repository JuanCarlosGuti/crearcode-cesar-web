package com.crearcode.leads.dominio;

/**
 * El logo de la marca, armado con piezas (decisión 35): una forma, un
 * símbolo —un emoji, o vacío para usar la inicial del nombre— y un
 * estilo de letra. El sitio lo dibuja con los colores de la paleta.
 */
public record LogoDeMarca(FormaDeLogo forma, String simbolo, EstiloDeLetra letra) {

	public LogoDeMarca {
		if (forma == null || letra == null) {
			throw new BocetoInvalidoException("El logo necesita su forma y su estilo de letra");
		}
		simbolo = EmojiDeBoceto.validado(simbolo, "El símbolo del logo");
	}

}
