package com.crearcode.leads.infraestructura.notificacion;

/**
 * Un correo listo para salir, independiente de cómo se transporte. Lo
 * comparten los tres adaptadores de correo del sitio.
 */
record CorreoSaliente(String para, String asunto, String cuerpo, Adjunto adjunto) {

	static CorreoSaliente simple(String para, String asunto, String cuerpo) {
		return new CorreoSaliente(para, asunto, cuerpo, null);
	}

	boolean tieneAdjunto() {
		return adjunto != null;
	}

	/** Único tipo de adjunto que envía el sitio: el PDF de una cotización. */
	record Adjunto(String nombre, byte[] contenido) {
	}

}
