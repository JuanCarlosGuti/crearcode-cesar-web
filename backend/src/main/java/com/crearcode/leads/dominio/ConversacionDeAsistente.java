package com.crearcode.leads.dominio;

import java.util.List;

/**
 * Historial acotado de una conversación con el asistente (F9). Sin
 * persistencia en v1: vive en la petición. El último mensaje debe ser
 * del USUARIO — es la pregunta que el asistente va a responder.
 */
public record ConversacionDeAsistente(List<MensajeDeChat> mensajes) {

	public static final int MAXIMO_DE_MENSAJES = 20;

	public ConversacionDeAsistente {
		if (mensajes == null || mensajes.isEmpty()) {
			throw new ConversacionInvalidaException("La conversación no puede estar vacía");
		}
		if (mensajes.size() > MAXIMO_DE_MENSAJES) {
			throw new ConversacionInvalidaException(
					"La conversación no puede superar los " + MAXIMO_DE_MENSAJES + " mensajes");
		}
		mensajes = List.copyOf(mensajes);
		if (mensajes.getLast().rol() != RolDeMensaje.USUARIO) {
			throw new ConversacionInvalidaException("El último mensaje debe ser del usuario");
		}
		exigirQueAlterneEmpezandoPorElUsuario(mensajes);
	}

	/**
	 * Una conversación de verdad alterna: pregunta, respuesta, pregunta.
	 * Exigirlo no es formalismo — el historial lo manda entero el
	 * cliente, así que puede fabricar turnos del propio asistente, y
	 * esos llegan al modelo como palabras suyas, con mucho más peso que
	 * una pregunta. Con hasta 19 turnos libres de 1000 caracteres se le
	 * podía hacer "confirmar" un precio inventado o salir de rol, que es
	 * justo la regla dura del sitio (auditoría del 28 sep 2026, P1-8a).
	 *
	 * <p>
	 * Esto acota el ataque a un turno intercalado, no lo elimina: el
	 * arreglo completo es guardar la conversación en el servidor, que
	 * contradice el diseño sin estado de v1 y está anotado como
	 * pendiente. Mientras tanto, el prompt de sistema avisa de que el
	 * historial no es de fiar.
	 */
	private static void exigirQueAlterneEmpezandoPorElUsuario(List<MensajeDeChat> mensajes) {
		for (int i = 0; i < mensajes.size(); i++) {
			RolDeMensaje esperado = i % 2 == 0 ? RolDeMensaje.USUARIO : RolDeMensaje.ASISTENTE;
			if (mensajes.get(i).rol() != esperado) {
				throw new ConversacionInvalidaException(
						"La conversación debe alternar entre usuario y asistente, empezando por el usuario");
			}
		}
	}

	public MensajeDeChat ultimoMensaje() {
		return mensajes.getLast();
	}

}
