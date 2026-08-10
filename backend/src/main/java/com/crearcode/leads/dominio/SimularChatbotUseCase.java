package com.crearcode.leads.dominio;

/**
 * Puerto de entrada del simulador "un chatbot para tu negocio"
 * (F10b, HU-40): responde como el chatbot que el negocio descrito
 * podría tener, con límites diarios propios.
 */
public interface SimularChatbotUseCase {

	RespuestaDelAsistente simular(NegocioSimulado negocio, ConversacionDeAsistente conversacion,
			IdentidadDelVisitante identidad);

}
