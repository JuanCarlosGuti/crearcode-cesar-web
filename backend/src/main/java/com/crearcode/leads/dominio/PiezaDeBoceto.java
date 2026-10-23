package com.crearcode.leads.dominio;

import java.util.List;

/**
 * Una pieza de una pantalla del boceto: su tipo, un título (obligatorio
 * para los tipos que lo necesitan) y hasta seis elementos.
 */
public record PiezaDeBoceto(TipoDePieza tipo, String titulo, List<ElementoDePieza> elementos) {

	public static final int MAXIMO_TITULO = 60;
	public static final int MAXIMO_ELEMENTOS = 6;

	public PiezaDeBoceto {
		if (tipo == null) {
			throw new BocetoInvalidoException("Una pieza necesita su tipo");
		}
		titulo = tipo.exigeTitulo()
				? TextoDeBoceto.obligatorio(titulo, MAXIMO_TITULO, "El título de " + tipo)
				: TextoDeBoceto.opcional(titulo, MAXIMO_TITULO, "El título de " + tipo);
		elementos = elementos == null ? List.of() : List.copyOf(elementos);
		if (elementos.size() > MAXIMO_ELEMENTOS) {
			throw new BocetoInvalidoException("Una pieza lleva como mucho %d elementos".formatted(MAXIMO_ELEMENTOS));
		}
		if (tipo.exigeElementos() && elementos.isEmpty()) {
			throw new BocetoInvalidoException(tipo + " necesita al menos un elemento");
		}
	}

}
