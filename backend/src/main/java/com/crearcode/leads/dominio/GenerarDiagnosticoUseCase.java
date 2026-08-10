package com.crearcode.leads.dominio;

/**
 * Puerto de entrada del diagnóstico digital (F10c, HU-41).
 */
public interface GenerarDiagnosticoUseCase {

	InformeDeDiagnostico generar(RespuestasDeDiagnostico respuestas, IdentidadDelVisitante identidad);

}
