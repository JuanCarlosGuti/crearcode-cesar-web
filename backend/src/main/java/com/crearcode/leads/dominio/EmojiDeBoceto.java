package com.crearcode.leads.dominio;

/**
 * El único texto libre del boceto que no es un texto (decisión 35): el
 * símbolo del logo y el ícono de cada elemento. Solo pasa si es un emoji
 * de verdad —letras, números o etiquetas no—, así que por ahí no puede
 * colarse nada más. Vacío es válido: el logo usa la inicial y el
 * elemento va sin dibujo.
 *
 * <p>
 * Acepta los emojis compuestos que se ven como uno solo: con selector
 * de variación (✂️), con unión invisible (👩‍🍳) y las banderas (🇨🇴).
 */
final class EmojiDeBoceto {

	/** Alcanza para 👩‍🍳 (5) o una bandera (4), no para una fila de emojis. */
	private static final int MAXIMO = 12;
	private static final int UNION_INVISIBLE = 0x200D;
	private static final int SELECTOR_DE_VARIACION = 0xFE0F;

	private EmojiDeBoceto() {
	}

	static String validado(String valor, String campo) {
		String limpio = valor == null ? "" : valor.strip();
		if (!limpio.isEmpty() && !esEmoji(limpio)) {
			throw new BocetoInvalidoException(campo + " tiene que ser un emoji");
		}
		return limpio;
	}

	static boolean esEmoji(String valor) {
		if (valor.length() > MAXIMO) {
			return false;
		}
		boolean tieneDibujo = valor.codePoints()
				.anyMatch(punto -> Character.isExtendedPictographic(punto) || Character.isEmojiPresentation(punto));
		boolean todoEsEmoji = valor.codePoints()
				.allMatch(punto -> Character.isEmoji(punto) || Character.isEmojiComponent(punto)
						|| punto == UNION_INVISIBLE || punto == SELECTOR_DE_VARIACION);
		return tieneDibujo && todoEsEmoji;
	}

}
