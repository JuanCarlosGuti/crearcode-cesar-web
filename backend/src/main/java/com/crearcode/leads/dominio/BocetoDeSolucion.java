package com.crearcode.leads.dominio;

import java.util.List;

/**
 * El boceto de la solución que arma la IA para el negocio del visitante
 * (decisión 33, ADR-16): un título, las funcionalidades sugeridas y de
 * dos a tres pantallas armadas con piezas de un catálogo cerrado. No es
 * un dibujo: el sitio lo dibuja, y nada de lo que trae se pinta como
 * HTML.
 *
 * <p>
 * La salida estructurada de la API garantiza la forma del JSON, pero no
 * los largos ni las cantidades (no admite esas restricciones en el
 * esquema): por eso las reglas viven aquí.
 */
public record BocetoDeSolucion(String titulo, List<String> funcionalidades, List<PantallaDeBoceto> pantallas) {

	public static final int MAXIMO_TITULO = 80;
	public static final int MAXIMO_FUNCIONALIDAD = 120;
	public static final int MINIMO_FUNCIONALIDADES = 3;
	public static final int MAXIMO_FUNCIONALIDADES = 6;
	public static final int MINIMO_PANTALLAS = 2;
	public static final int MAXIMO_PANTALLAS = 3;

	public BocetoDeSolucion {
		titulo = TextoDeBoceto.obligatorio(titulo, MAXIMO_TITULO, "El título del boceto");
		funcionalidades = (funcionalidades == null ? List.<String>of() : funcionalidades).stream()
				.map(funcionalidad -> TextoDeBoceto.obligatorio(funcionalidad, MAXIMO_FUNCIONALIDAD,
						"Una funcionalidad"))
				.toList();
		if (funcionalidades.size() < MINIMO_FUNCIONALIDADES || funcionalidades.size() > MAXIMO_FUNCIONALIDADES) {
			throw new BocetoInvalidoException("El boceto lleva de %d a %d funcionalidades"
					.formatted(MINIMO_FUNCIONALIDADES, MAXIMO_FUNCIONALIDADES));
		}
		pantallas = pantallas == null ? List.of() : List.copyOf(pantallas);
		if (pantallas.size() < MINIMO_PANTALLAS || pantallas.size() > MAXIMO_PANTALLAS) {
			throw new BocetoInvalidoException(
					"El boceto lleva de %d a %d pantallas".formatted(MINIMO_PANTALLAS, MAXIMO_PANTALLAS));
		}
	}

}
