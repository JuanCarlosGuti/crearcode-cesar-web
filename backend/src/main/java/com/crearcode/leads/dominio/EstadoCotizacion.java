package com.crearcode.leads.dominio;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados de una {@link Cotizacion}. ACEPTADA, RECHAZADA,
 * VENCIDA y CANCELADA son terminales: no aparecen como origen en
 * {@link #TRANSICIONES_VALIDAS}, así que ninguna transición fuera de
 * ellas es válida (ni siquiera hacia sí mismas).
 */
public enum EstadoCotizacion {
	BORRADOR,
	ENVIADA,
	ACEPTADA,
	RECHAZADA,
	VENCIDA,
	CANCELADA;

	private static final Map<EstadoCotizacion, Set<EstadoCotizacion>> TRANSICIONES_VALIDAS =
			new EnumMap<>(Map.of(
					BORRADOR, EnumSet.of(ENVIADA, CANCELADA),
					ENVIADA, EnumSet.of(ACEPTADA, RECHAZADA, VENCIDA, CANCELADA)));

	public boolean puedeTransicionarA(EstadoCotizacion destino) {
		return TRANSICIONES_VALIDAS.getOrDefault(this, Set.of()).contains(destino);
	}

}
