package com.crearcode.leads.dominio;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * El catálogo cerrado de piezas con las que se arma una pantalla del
 * boceto (ADR-16). Cada tipo dice qué necesita para poder dibujarse:
 * un botón sin texto o una lista sin filas no se pueden pintar.
 */
public enum TipoDePieza {

	ENCABEZADO(true, false),
	BANNER(true, false),
	BUSCADOR(true, false),
	CATEGORIAS(false, true),
	TARJETAS(false, true),
	LISTA(false, true),
	FORMULARIO(false, true),
	BOTON(true, false),
	INDICADORES(false, true),
	CHAT(false, true),
	HORARIOS(false, true),
	NAVEGACION(false, true);

	private final boolean exigeTitulo;
	private final boolean exigeElementos;

	TipoDePieza(boolean exigeTitulo, boolean exigeElementos) {
		this.exigeTitulo = exigeTitulo;
		this.exigeElementos = exigeElementos;
	}

	boolean exigeTitulo() {
		return exigeTitulo;
	}

	boolean exigeElementos() {
		return exigeElementos;
	}

	/** Sin importar mayúsculas: la API no las garantiza en un enum. */
	public static Optional<TipoDePieza> desde(String nombre) {
		if (nombre == null) {
			return Optional.empty();
		}
		String buscado = nombre.trim().toUpperCase(Locale.ROOT);
		return Arrays.stream(values()).filter(tipo -> tipo.name().equals(buscado)).findFirst();
	}

}
