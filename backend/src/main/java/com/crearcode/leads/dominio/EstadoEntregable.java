package com.crearcode.leads.dominio;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados de un entregable del proyecto (fase F12, docs/03
 * Parte 5 §4). APROBADO es terminal: no aparece como origen en
 * {@link #TRANSICIONES_VALIDAS}. Pasar a CON_AJUSTES exige la nota de
 * qué se ajusta, pero eso lo exige el entregable, no el enum.
 */
public enum EstadoEntregable {
	PENDIENTE,
	EN_CURSO,
	EN_REVISION,
	CON_AJUSTES,
	APROBADO;

	private static final Map<EstadoEntregable, Set<EstadoEntregable>> TRANSICIONES_VALIDAS =
			new EnumMap<>(Map.of(
					PENDIENTE, EnumSet.of(EN_CURSO),
					EN_CURSO, EnumSet.of(EN_REVISION),
					EN_REVISION, EnumSet.of(APROBADO, CON_AJUSTES),
					CON_AJUSTES, EnumSet.of(EN_CURSO)));

	public boolean puedeTransicionarA(EstadoEntregable destino) {
		return TRANSICIONES_VALIDAS.getOrDefault(this, Set.of()).contains(destino);
	}

	public boolean esTerminal() {
		return !TRANSICIONES_VALIDAS.containsKey(this);
	}

	/**
	 * Decisión 27 de docs/10: las dos salidas de EN_REVISION —aprobar o
	 * pedir ajustes— las puede tomar el cliente desde su cuenta; las
	 * demás transiciones son solo del equipo.
	 */
	public boolean esperaRespuestaDelCliente() {
		return this == EN_REVISION;
	}

}
