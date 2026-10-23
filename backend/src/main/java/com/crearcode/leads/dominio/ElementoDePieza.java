package com.crearcode.leads.dominio;

/**
 * Una fila, tarjeta, campo o mensaje dentro de una pieza del boceto:
 * un texto principal y un detalle que puede ir vacío.
 */
public record ElementoDePieza(String texto, String detalle) {

	public static final int MAXIMO_TEXTO = 60;
	public static final int MAXIMO_DETALLE = 80;

	public ElementoDePieza {
		texto = TextoDeBoceto.obligatorio(texto, MAXIMO_TEXTO, "El texto de un elemento");
		detalle = TextoDeBoceto.opcional(detalle, MAXIMO_DETALLE, "El detalle de un elemento");
	}

}
