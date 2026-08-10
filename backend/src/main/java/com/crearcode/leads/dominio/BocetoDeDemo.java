package com.crearcode.leads.dominio;

import java.util.List;

/**
 * El resultado del demo de diseño (F10d, HU-42): título de la
 * solución, funcionalidades sugeridas y la imagen del boceto — que se
 * muestra SOLO como imagen, nunca HTML ejecutable.
 */
public record BocetoDeDemo(String titulo, List<String> funcionalidades, ImagenGenerada imagen) {

	public static final int MINIMO_FUNCIONALIDADES = 3;
	public static final int MAXIMO_FUNCIONALIDADES = 6;

	public BocetoDeDemo {
		titulo = titulo == null ? "" : titulo.trim();
		funcionalidades = List.copyOf(funcionalidades);
		if (titulo.isEmpty()) {
			throw new SolicitudDeDemoInvalidaException("El boceto requiere un título");
		}
		if (funcionalidades.size() < MINIMO_FUNCIONALIDADES
				|| funcionalidades.size() > MAXIMO_FUNCIONALIDADES) {
			throw new SolicitudDeDemoInvalidaException(
					"El boceto requiere entre %d y %d funcionalidades"
							.formatted(MINIMO_FUNCIONALIDADES, MAXIMO_FUNCIONALIDADES));
		}
	}

}
