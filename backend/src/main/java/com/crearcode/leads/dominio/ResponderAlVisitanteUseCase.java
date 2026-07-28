package com.crearcode.leads.dominio;

/** Caso de uso del chat del asistente (F9, HU-36 y HU-38). */
public interface ResponderAlVisitanteUseCase {

	RespuestaDelAsistente responder(ConversacionDeAsistente conversacion, IdentidadDelVisitante identidad);

}
