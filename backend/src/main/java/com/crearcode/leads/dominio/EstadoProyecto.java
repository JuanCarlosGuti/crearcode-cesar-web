package com.crearcode.leads.dominio;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados de un {@code Proyecto} (fase F12, docs/03 Parte 5
 * §4). CERRADO es terminal. {@code EN_GARANTIA -> ACTIVO} existe porque un
 * cambio de alcance agregado durante la garantía (decisión 22) vuelve a
 * dejar trabajo pendiente, y un proyecto con trabajo pendiente no está en
 * garantía.
 */
public enum EstadoProyecto {
	ACTIVO,
	PAUSADO,
	EN_GARANTIA,
	CERRADO;

	private static final Map<EstadoProyecto, Set<EstadoProyecto>> TRANSICIONES_VALIDAS =
			new EnumMap<>(Map.of(
					ACTIVO, EnumSet.of(PAUSADO, EN_GARANTIA),
					PAUSADO, EnumSet.of(ACTIVO),
					EN_GARANTIA, EnumSet.of(ACTIVO, CERRADO)));

	public boolean puedeTransicionarA(EstadoProyecto destino) {
		return TRANSICIONES_VALIDAS.getOrDefault(this, Set.of()).contains(destino);
	}

	/**
	 * Invariante 8: mientras el proyecto está pausado o cerrado sus
	 * entregables no cambian de estado. En garantía tampoco: todos están
	 * aprobados, y el único trabajo nuevo posible —un cambio de alcance—
	 * lo devuelve primero a ACTIVO.
	 */
	public boolean permiteMoverEntregables() {
		return this == ACTIVO;
	}

}
