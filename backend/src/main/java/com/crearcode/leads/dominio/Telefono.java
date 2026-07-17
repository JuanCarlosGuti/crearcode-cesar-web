package com.crearcode.leads.dominio;

import java.util.regex.Pattern;

/**
 * Value object del teléfono de contacto. Almacena siempre la forma
 * canónica de 10 dígitos (celular colombiano, sin prefijo de país) para
 * que dos entradas equivalentes ("+57 300...", "57 300...", "300...")
 * sean iguales por valor.
 */
public record Telefono(String valor) {

	private static final Pattern CELULAR_COLOMBIANO = Pattern.compile("^3\\d{9}$");

	public Telefono {
		if (valor == null || valor.isBlank()) {
			throw new DatosDeContactoInvalidosException("El teléfono no puede estar vacío");
		}

		String normalizado = normalizar(valor);
		if (!CELULAR_COLOMBIANO.matcher(normalizado).matches()) {
			throw new DatosDeContactoInvalidosException(
					"El teléfono no es un celular colombiano válido: " + valor);
		}
		valor = normalizado;
	}

	private static String normalizar(String valorOriginal) {
		String soloDigitos = valorOriginal.replaceAll("[\\s\\-]", "").replaceFirst("^\\+", "");
		if (soloDigitos.startsWith("57") && soloDigitos.length() == 12) {
			soloDigitos = soloDigitos.substring(2);
		}
		return soloDigitos;
	}

}
