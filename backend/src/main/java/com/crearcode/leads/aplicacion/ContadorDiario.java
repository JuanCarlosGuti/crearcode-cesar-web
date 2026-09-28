package com.crearcode.leads.aplicacion;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contador por día compartido por el asistente (F9) y el simulador
 * (F10b): al cambiar la fecha se descartan los contadores del día
 * anterior (no hay reseteo programado — se comparan fechas). En
 * memoria: suficiente para la instancia única de v1.
 */
final class ContadorDiario {

	private volatile LocalDate dia = LocalDate.MIN;
	private final Map<String, AtomicInteger> contadores = new ConcurrentHashMap<>();

	private synchronized void reiniciarSiCambioElDia(LocalDate hoy) {
		if (!hoy.equals(dia)) {
			contadores.clear();
			dia = hoy;
		}
	}

	/**
	 * Incrementa y devuelve el valor nuevo en UNA sola operacion. Leer
	 * primero y sumar despues dejaba pasar a todos los que llegaban a la
	 * vez (auditoria del 28 sep 2026): con 6 de cupo, 18 peticiones
	 * simultaneas llegaban las 18 al proveedor.
	 */
	int reservar(LocalDate hoy, String clave) {
		reiniciarSiCambioElDia(hoy);
		return contadores.computeIfAbsent(clave, ignorada -> new AtomicInteger()).incrementAndGet();
	}

	/** Devuelve una reserva que no se uso: limite superado o proveedor caido. Nunca baja de cero. */
	void liberar(LocalDate hoy, String clave) {
		reiniciarSiCambioElDia(hoy);
		AtomicInteger contador = contadores.get(clave);
		if (contador != null) {
			contador.updateAndGet(valor -> Math.max(0, valor - 1));
		}
	}

}
