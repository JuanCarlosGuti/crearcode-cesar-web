package com.crearcode.leads.infraestructura.demo;

import java.util.Map;
import java.util.Optional;

/**
 * Lo que cuesta cada llamada, para que el log permita sumar el gasto del
 * mes contra los créditos del usuario (decisión 33). Es una estimación:
 * la cifra que manda es la de la Consola de Anthropic.
 *
 * <p>
 * Precios en dólares por millón de tokens, verificados el 23 oct 2026.
 * Escribir en la caché de 5 minutos cuesta 1,25 veces la entrada. Un
 * modelo que no esté aquí registra los tokens sin costo.
 */
final class PreciosDeClaude {

	private static final double RECARGO_ESCRITURA_DE_CACHE = 1.25;
	private static final double MILLON = 1_000_000.0;

	private record Precio(double entrada, double salida, double lecturaDeCache) {
	}

	private static final Map<String, Precio> PRECIOS = Map.of(
			"claude-haiku-5-5", new Precio(0.10, 0.50, 0.01),
			"claude-sonnet-5-5", new Precio(2, 10, 0.20),
			"claude-opus-5-5", new Precio(4, 20, 0.20),
			"claude-fable-5-1", new Precio(10, 50, 0.25));

	private PreciosDeClaude() {
	}

	static Optional<Double> costoEstimado(String modelo, long entrada, long salida, long cacheEscrita,
			long cacheLeida) {
		Precio precio = PRECIOS.get(modelo);
		if (precio == null) {
			return Optional.empty();
		}
		return Optional.of((entrada * precio.entrada()
				+ cacheEscrita * precio.entrada() * RECARGO_ESCRITURA_DE_CACHE
				+ cacheLeida * precio.lecturaDeCache()
				+ salida * precio.salida()) / MILLON);
	}

}
