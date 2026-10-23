package com.crearcode.leads.dominio;

import java.util.List;

/** Una pantalla del boceto: su nombre corto y de dos a ocho piezas. */
public record PantallaDeBoceto(String nombre, List<PiezaDeBoceto> piezas) {

	public static final int MAXIMO_NOMBRE = 30;
	public static final int MINIMO_PIEZAS = 2;
	public static final int MAXIMO_PIEZAS = 8;

	public PantallaDeBoceto {
		nombre = TextoDeBoceto.obligatorio(nombre, MAXIMO_NOMBRE, "El nombre de una pantalla");
		piezas = piezas == null ? List.of() : List.copyOf(piezas);
		if (piezas.size() < MINIMO_PIEZAS || piezas.size() > MAXIMO_PIEZAS) {
			throw new BocetoInvalidoException(
					"Una pantalla lleva de %d a %d piezas".formatted(MINIMO_PIEZAS, MAXIMO_PIEZAS));
		}
	}

}
