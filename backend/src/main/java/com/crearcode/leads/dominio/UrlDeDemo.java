package com.crearcode.leads.dominio;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Enlace a la demo de un entregable, el que el portal pinta como botón
 * "Ver demo". Solo {@code https://}, con dominio y sin usuario antes del
 * dominio: un enlace pegado en el panel es la puerta clásica a un
 * {@code javascript:} o a un {@code https://nuestro.com@otro.co} que
 * parece nuestro y no lo es. Se cierra aquí y no en la interfaz, que es
 * donde se olvida.
 */
public record UrlDeDemo(String valor) {

	private static final int LONGITUD_MAXIMA = 500;

	public UrlDeDemo {
		if (valor == null || valor.isBlank()) {
			throw new ProyectoInvalidoException("El enlace de la demo no puede estar vacío");
		}
		valor = valor.trim();
		if (valor.length() > LONGITUD_MAXIMA) {
			throw new ProyectoInvalidoException("El enlace de la demo supera los " + LONGITUD_MAXIMA + " caracteres");
		}
		URI uri = interpretar(valor);
		if (!"https".equalsIgnoreCase(uri.getScheme())) {
			throw new ProyectoInvalidoException("El enlace de la demo tiene que empezar por https://");
		}
		if (uri.getHost() == null || uri.getHost().isBlank()) {
			throw new ProyectoInvalidoException("El enlace de la demo no tiene dominio");
		}
		if (uri.getRawUserInfo() != null) {
			throw new ProyectoInvalidoException("El enlace de la demo no puede llevar un usuario antes del dominio");
		}
	}

	private static URI interpretar(String valor) {
		try {
			return new URI(valor);
		}
		catch (URISyntaxException excepcion) {
			throw new ProyectoInvalidoException("El enlace de la demo no es una dirección válida");
		}
	}

}
