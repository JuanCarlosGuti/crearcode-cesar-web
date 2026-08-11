package com.crearcode.leads.dominio;

import java.util.List;

/**
 * Comando para abrir un borrador de cotización. O bien nace de un lead
 * ({@code origen}, y entonces los datos del cliente salen de la
 * solicitud), o bien se abre en blanco y {@code cliente} es obligatorio.
 *
 * <p>{@code diasDeValidez} en 0 significa "usa el valor por defecto de
 * la configuración".
 */
public record NuevoBorrador(SolicitudId origen, DatosDelCliente cliente, Porcentaje impuesto,
		int diasDeValidez, String notas, List<ItemDeCotizacion> items) {

	public NuevoBorrador {
		items = items == null ? List.of() : List.copyOf(items);
		if (origen == null && cliente == null) {
			throw new CotizacionInvalidaException(
					"Una cotización en blanco necesita los datos del cliente");
		}
		if (diasDeValidez < 0) {
			throw new CotizacionInvalidaException("Los días de validez no pueden ser negativos");
		}
	}

}
