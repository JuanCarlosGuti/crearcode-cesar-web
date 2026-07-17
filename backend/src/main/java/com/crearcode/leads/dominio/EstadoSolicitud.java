package com.crearcode.leads.dominio;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados de una {@link SolicitudDeContacto}. CONVERTIDA y
 * DESCARTADA son terminales: no aparecen como origen en
 * {@link #TRANSICIONES_VALIDAS}, por lo que ninguna transición fuera de
 * ellas se considera válida (ni siquiera hacia sí mismas).
 */
public enum EstadoSolicitud {
	NUEVA,
	CONTACTADA,
	CONVERTIDA,
	DESCARTADA;

	private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> TRANSICIONES_VALIDAS =
			new EnumMap<>(Map.of(
					NUEVA, EnumSet.of(CONTACTADA, DESCARTADA),
					CONTACTADA, EnumSet.of(CONVERTIDA, DESCARTADA)));

	public boolean puedeTransicionarA(EstadoSolicitud destino) {
		return TRANSICIONES_VALIDAS.getOrDefault(this, Set.of()).contains(destino);
	}

}
