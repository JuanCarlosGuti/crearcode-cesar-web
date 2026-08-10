package com.crearcode.leads.dominio;

/**
 * Puerto de entrada del demo de diseño con IA (F10d, HU-42): SOLO
 * para identidades registradas.
 */
public interface GenerarDemoDeDisenoUseCase {

	BocetoDeDemo generar(SolicitudDeDemo solicitud, IdentidadDelVisitante identidad);

}
