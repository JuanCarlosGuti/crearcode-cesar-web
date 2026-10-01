package com.crearcode.leads.dominio;

/**
 * Un monto de {@link Dinero} que no puede existir: nulo o negativo.
 *
 * <p>Hasta la fase F12 {@code Dinero} lanzaba
 * {@link CotizacionInvalidaException}, porque solo lo usaban las
 * cotizaciones. Desde que también lo usan los proyectos y sus pagos, un
 * pago negativo no puede llamarse "cotización inválida".
 */
public class MontoInvalidoException extends RuntimeException {

	public MontoInvalidoException(String mensaje) {
		super(mensaje);
	}

}
