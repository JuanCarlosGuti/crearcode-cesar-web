package com.crearcode.leads.dominio;

import java.time.LocalDate;

/**
 * Lo editable de una fase (un sprint): nombre, objetivo en 1-2 frases no
 * técnicas y fechas planeadas, que pueden quedar abiertas mientras se
 * arma el plan.
 */
public record DatosDeFase(String nombre, String objetivo, LocalDate inicioPlaneado, LocalDate finPlaneado) {

	public DatosDeFase {
		nombre = TextoDeProyecto.obligatorio(nombre, 120, "El nombre de la fase");
		objetivo = TextoDeProyecto.opcional(objetivo, 500, "El objetivo de la fase");
		if (inicioPlaneado != null && finPlaneado != null && finPlaneado.isBefore(inicioPlaneado)) {
			throw new ProyectoInvalidoException("La fase no puede terminar antes de empezar");
		}
	}

}
