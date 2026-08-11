package com.crearcode.leads.dominio;

/**
 * Línea de la cotización. El subtotal se calcula, nunca se almacena ni
 * se recibe de fuera (invariante 2 del contexto): así ninguna capa
 * puede colar un total que no salga de cantidad × valor unitario.
 */
public record ItemDeCotizacion(String descripcion, int cantidad, Dinero valorUnitario) {

	private static final int LONGITUD_MAXIMA_DESCRIPCION = 200;

	public ItemDeCotizacion {
		if (descripcion == null || descripcion.isBlank()) {
			throw new CotizacionInvalidaException("La descripción del ítem no puede estar vacía");
		}
		descripcion = descripcion.trim();
		if (descripcion.length() > LONGITUD_MAXIMA_DESCRIPCION) {
			throw new CotizacionInvalidaException("La descripción del ítem supera la longitud máxima permitida");
		}
		if (cantidad <= 0) {
			throw new CotizacionInvalidaException("La cantidad debe ser mayor que cero: " + cantidad);
		}
		if (valorUnitario == null) {
			throw new CotizacionInvalidaException("El ítem necesita un valor unitario");
		}
	}

	public Dinero subtotal() {
		return valorUnitario.por(cantidad);
	}

}
