package com.crearcode.leads.dominio;

import java.util.List;

/** Una fase del plan inicial, con sus entregables, al crear el proyecto. */
public record NuevaFase(DatosDeFase datos, List<DatosDeEntregable> entregables) {

	public NuevaFase {
		if (datos == null) {
			throw new ProyectoInvalidoException("La fase necesita sus datos");
		}
		if (entregables == null || entregables.stream().anyMatch(java.util.Objects::isNull)) {
			throw new ProyectoInvalidoException("La lista de entregables no puede ser nula ni contener nulos");
		}
		entregables = List.copyOf(entregables);
	}

}
