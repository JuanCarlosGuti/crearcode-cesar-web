package com.crearcode.leads.dominio;

/**
 * Puerto de salida hacia el proveedor de IA (ADR-10). La
 * infraestructura lo implementa con Groq; cambiar de proveedor es
 * escribir otro adaptador, sin tocar dominio ni aplicación.
 */
public interface GeneradorDeRespuestas {

	/**
	 * @throws AsistenteNoDisponibleException si el proveedor falla o está
	 *                                        saturado — la aplicación la
	 *                                        traduce a la respuesta de
	 *                                        indisponibilidad, nunca llega
	 *                                        al visitante como error
	 *                                        técnico.
	 */
	RespuestaDelAsistente responder(ConversacionDeAsistente conversacion);

}
