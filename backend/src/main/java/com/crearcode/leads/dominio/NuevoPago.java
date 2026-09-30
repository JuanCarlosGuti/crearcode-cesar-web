package com.crearcode.leads.dominio;

import java.time.LocalDate;

/**
 * Un pago recibido, tal como lo registra el equipo (decisión 24). Todo
 * pago es mayor que cero; la referencia (número o nota del comprobante)
 * es opcional.
 */
public record NuevoPago(EntregableId entregable, Dinero monto, LocalDate fecha, MedioDePago medio,
		OrigenDePago origen, String referencia, Correo registradoPor) {

	public NuevoPago {
		if (entregable == null) {
			throw new ProyectoInvalidoException("El pago tiene que ser de un entregable");
		}
		if (monto == null || monto.esCero()) {
			throw new ProyectoInvalidoException("El pago tiene que ser mayor que cero");
		}
		if (fecha == null) {
			throw new ProyectoInvalidoException("El pago necesita su fecha");
		}
		if (medio == null) {
			throw new ProyectoInvalidoException("El pago necesita su medio");
		}
		if (origen == null) {
			throw new ProyectoInvalidoException("El pago necesita su origen");
		}
		if (registradoPor == null) {
			throw new ProyectoInvalidoException("El pago necesita quién lo registró");
		}
		referencia = TextoDeProyecto.opcional(referencia, 200, "La referencia del pago");
	}

}
