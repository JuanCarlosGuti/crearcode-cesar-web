package com.crearcode.leads.dominio;

import java.time.LocalDate;

/** Un pago ya registrado en el proyecto. */
public record Pago(PagoId id, EntregableId entregable, Dinero monto, LocalDate fecha, MedioDePago medio,
		OrigenDePago origen, String referencia, Correo registradoPor) {

	static Pago desde(NuevoPago nuevo) {
		return new Pago(PagoId.nuevo(), nuevo.entregable(), nuevo.monto(), nuevo.fecha(), nuevo.medio(),
				nuevo.origen(), nuevo.referencia(), nuevo.registradoPor());
	}

}
