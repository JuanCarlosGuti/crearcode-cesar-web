package com.crearcode.leads.dominio;

/**
 * Una fila, tarjeta, campo o mensaje dentro de una pieza del boceto: un
 * texto principal, un detalle que puede ir vacío y, desde la decisión
 * 35, un ícono —un emoji o vacío— que hace de dibujo del producto.
 */
public record ElementoDePieza(String texto, String detalle, String icono) {

	public static final int MAXIMO_TEXTO = 60;
	public static final int MAXIMO_DETALLE = 80;

	public ElementoDePieza {
		texto = TextoDeBoceto.obligatorio(texto, MAXIMO_TEXTO, "El texto de un elemento");
		detalle = TextoDeBoceto.opcional(detalle, MAXIMO_DETALLE, "El detalle de un elemento");
		icono = EmojiDeBoceto.validado(icono, "El ícono de un elemento");
	}

	/** Un elemento sin dibujo. */
	public ElementoDePieza(String texto, String detalle) {
		this(texto, detalle, "");
	}

}
