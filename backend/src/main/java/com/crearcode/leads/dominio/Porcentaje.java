package com.crearcode.leads.dominio;

/**
 * Value object de un porcentaje entero (0-100). Se usa para el impuesto
 * de la cotización; **cero es un valor válido** mientras la empresa
 * confirma su condición de IVA (dato pendiente en [[05-backlog-issues]]).
 */
public record Porcentaje(int valor) {

	public Porcentaje {
		if (valor < 0 || valor > 100) {
			throw new CotizacionInvalidaException("El porcentaje debe estar entre 0 y 100: " + valor);
		}
	}

}
