package com.crearcode.leads.dominio;

import java.util.regex.Pattern;

/**
 * Value object del correo de contacto. Validación de formato simplificada
 * (no RFC completa) a propósito: suficiente para rechazar entradas
 * claramente inválidas sin la complejidad de un parser RFC 5322 completo.
 */
public record Correo(String valor) {

	private static final int LONGITUD_MAXIMA = 254;
	private static final Pattern FORMATO_VALIDO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	public Correo {
		if (valor == null || valor.isBlank()) {
			throw new DatosDeContactoInvalidosException("El correo no puede estar vacío");
		}
		if (valor.length() > LONGITUD_MAXIMA) {
			throw new DatosDeContactoInvalidosException("El correo supera la longitud máxima permitida");
		}
		if (!FORMATO_VALIDO.matcher(valor).matches()) {
			throw new DatosDeContactoInvalidosException("El correo no tiene un formato válido: " + valor);
		}
	}

}
