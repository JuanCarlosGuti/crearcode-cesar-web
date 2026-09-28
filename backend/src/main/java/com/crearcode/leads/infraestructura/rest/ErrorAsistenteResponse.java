package com.crearcode.leads.infraestructura.rest;

/**
 * Error del asistente con un código estable para que la interfaz
 * decida qué mostrar: {@code limite-anonimo} (CTA a registro),
 * {@code limite-registrado}, {@code solo-registrados},
 * {@code limite-global} (el cupo del día se agotó: vuelve mañana) o
 * {@code proveedor-caido} (avería: alternativa humana ya). Los dos
 * últimos eran el mismo código hasta la auditoría del 28 sep 2026, y
 * eso impedía distinguir un día de mucho tráfico de un proveedor
 * roto. Los textos visibles viven en el frontend (ADR-05).
 */
record ErrorAsistenteResponse(String mensaje, String codigo) {

}
