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

	int valor(LocalDate hoy, String clave) {
		reiniciarSiCambioElDia(hoy);
		AtomicInteger contador = contadores.get(clave);
		return contador == null ? 0 : contador.get();
	}

	void incrementar(LocalDate hoy, String clave) {
		reiniciarSiCambioElDia(hoy);
		contadores.computeIfAbsent(clave, ignorada -> new AtomicInteger()).incrementAndGet();
	}

}
