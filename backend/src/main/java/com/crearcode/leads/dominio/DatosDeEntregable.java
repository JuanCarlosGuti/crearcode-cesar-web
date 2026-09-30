package com.crearcode.leads.dominio;

/**
 * Lo editable de un entregable: nombre, descripción para el cliente,
 * valor antes de impuesto y cuándo se cobra (decisión 21).
 */
public record DatosDeEntregable(String nombre, String descripcion, Dinero valor, MomentoDeCobro cobro) {

	public DatosDeEntregable {
		nombre = TextoDeProyecto.obligatorio(nombre, 120, "El nombre del entregable");
		descripcion = TextoDeProyecto.opcional(descripcion, 1000, "La descripción del entregable");
		if (valor == null) {
			throw new ProyectoInvalidoException("El entregable necesita un valor (puede ser cero)");
		}
		if (cobro == null) {
			throw new ProyectoInvalidoException("El entregable necesita saber cuándo se cobra");
		}
	}

}
