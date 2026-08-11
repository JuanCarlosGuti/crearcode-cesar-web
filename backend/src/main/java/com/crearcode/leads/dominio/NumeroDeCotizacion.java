package com.crearcode.leads.dominio;

import java.util.regex.Pattern;

/**
 * Consecutivo de la cotización con formato {@code COT-AAAA-NNNN}. Se
 * asigna al ENVIAR, no al crear el borrador: un borrador que nunca sale
 * no consume número (invariante 5 del contexto).
 */
public record NumeroDeCotizacion(String valor) {

	private static final Pattern FORMATO_VALIDO = Pattern.compile("^COT-\\d{4}-\\d{4,}$");

	public NumeroDeCotizacion {
		if (valor == null || !FORMATO_VALIDO.matcher(valor).matches()) {
			throw new CotizacionInvalidaException("El número de cotización no tiene el formato COT-AAAA-NNNN: " + valor);
		}
	}

	public static NumeroDeCotizacion de(int anio, int consecutivo) {
		if (consecutivo <= 0) {
			throw new CotizacionInvalidaException("El consecutivo debe ser positivo: " + consecutivo);
		}
		return new NumeroDeCotizacion("COT-%d-%04d".formatted(anio, consecutivo));
	}

}
