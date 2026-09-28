package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Supplier;

import com.crearcode.leads.dominio.IdentidadDelVisitante;

/**
 * Los cupos diarios de una herramienta de IA: el global (el techo de
 * la capa gratis del proveedor) y el personal, mayor para quien tiene
 * cuenta (HU-38). Contadores en memoria por día — suficiente para la
 * instancia única de v1, igual que el rate limiting por IP.
 *
 * <p>
 * Las cuatro herramientas repetían esta regla palabra por palabra, con
 * su propia pareja de contadores cada una. Eran cuatro sitios donde
 * equivocarse con el orden de reserva y liberación, y de hecho los
 * cuatro tuvieron el mismo fallo hasta la auditoría del 28 sep 2026:
 * leían el contador, llamaban al proveedor y solo entonces sumaban, así
 * que una ráfaga simultánea pasaba entera.
 *
 * <p>
 * Dos reglas que no son obvias y por eso viven aquí y no repartidas:
 * el cupo se <b>reserva antes</b> de llamar al proveedor (si no, no hay
 * nada que impida la ráfaga) y se <b>devuelve</b> cuando no se usó —
 * porque se superó el límite personal, o porque el proveedor falló. Un
 * 503 no le cuesta consultas a nadie.
 */
final class CupoDeIa {

	private static final String CLAVE_GLOBAL = "global";

	private final Clock reloj;
	private final int limiteGlobalDiario;
	private final int limiteDiarioRegistrado;
	private final int limiteDiarioAnonimo;

	private final ContadorDiario contadorGlobal = new ContadorDiario();
	private final ContadorDiario contadorPorIdentidad = new ContadorDiario();

	CupoDeIa(Clock reloj, int limiteGlobalDiario, int limiteDiarioRegistrado, int limiteDiarioAnonimo) {
		this.reloj = reloj;
		this.limiteGlobalDiario = limiteGlobalDiario;
		this.limiteDiarioRegistrado = limiteDiarioRegistrado;
		this.limiteDiarioAnonimo = limiteDiarioAnonimo;
	}

	/**
	 * Ejecuta el trabajo si queda cupo; si no, lanza sin llegar a
	 * llamar al proveedor.
	 *
	 * @throws LimiteGlobalAlcanzadoException si se agotó el cupo del día
	 *                                        de la casa
	 * @throws LimiteDeUsoAlcanzadoException  si el visitante agotó el
	 *                                        suyo
	 */
	<T> T ejecutar(IdentidadDelVisitante identidad, Supplier<T> trabajo) {
		LocalDate hoy = LocalDate.now(reloj);
		int limitePersonal = identidad.registrada() ? limiteDiarioRegistrado : limiteDiarioAnonimo;

		if (contadorGlobal.reservar(hoy, CLAVE_GLOBAL) > limiteGlobalDiario) {
			contadorGlobal.liberar(hoy, CLAVE_GLOBAL);
			throw new LimiteGlobalAlcanzadoException();
		}
		if (contadorPorIdentidad.reservar(hoy, identidad.clave()) > limitePersonal) {
			liberarTodo(hoy, identidad);
			throw new LimiteDeUsoAlcanzadoException(identidad.registrada());
		}
		try {
			return trabajo.get();
		} catch (RuntimeException fallo) {
			liberarTodo(hoy, identidad);
			throw fallo;
		}
	}

	private void liberarTodo(LocalDate hoy, IdentidadDelVisitante identidad) {
		contadorPorIdentidad.liberar(hoy, identidad.clave());
		contadorGlobal.liberar(hoy, CLAVE_GLOBAL);
	}

}
