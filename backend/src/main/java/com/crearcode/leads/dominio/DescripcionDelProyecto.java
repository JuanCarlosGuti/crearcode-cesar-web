package com.crearcode.leads.dominio;

import java.time.LocalDate;

/**
 * Lo que el cliente lee arriba de su proyecto: nombre, descripción en su
 * idioma (no técnica) y las fechas de inicio y de entrega estimada.
 */
public record DescripcionDelProyecto(String nombre, String descripcion, LocalDate inicio,
		LocalDate entregaEstimada) {

	public DescripcionDelProyecto {
		nombre = TextoDeProyecto.obligatorio(nombre, 120, "El nombre del proyecto");
		descripcion = TextoDeProyecto.opcional(descripcion, 1000, "La descripción del proyecto");
		if (inicio == null) {
			throw new ProyectoInvalidoException("El proyecto necesita fecha de inicio");
		}
		if (entregaEstimada != null && entregaEstimada.isBefore(inicio)) {
			throw new ProyectoInvalidoException("La entrega estimada no puede ser antes del inicio");
		}
	}

}
